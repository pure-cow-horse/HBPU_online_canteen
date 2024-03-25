package com.itheima.reggie.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.AliPayConfig;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrderDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    private static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT = "JSON";
    private static final String CHARSET = "utf-8";
    private static final String SIGN_TYPE = "RSA2";
    private final String TRADE_FINISHED = "TRADE_FINISHED";
    private final String TRADE_SUCCESS = "TRADE_SUCCESS";
    @Resource
    AliPayConfig aliPayConfig;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Value("${alipay.notify_url}")
    private String notifyUrl;
    @Value("${alipay.return_url}")
    private String returnUrl;
    @Value("${alipay.charset}")
    private String charset;
    @Value("${alipay.alipay_public_key}")
    private String alipayPublicKey;
    @Value("${alipay.sign_type}")
    private String signType;

    // 示例：支付宝异步通知处理
    @PostMapping("/notify")
    public void notifyUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("异步通知");
        PrintWriter out = response.getWriter();
        //乱码解决，这段代码在出现乱码时使用
        request.setCharacterEncoding("utf-8");
        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<>(8);
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> stringEntry : requestParams.entrySet()) {
            String[] values = stringEntry.getValue();
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(stringEntry.getKey(), valueStr);
        }

        //调用SDK验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, charset, signType);

        if (!signVerified) {
            log.error("验签失败");
            out.print("fail");
            return;
        }

        //商户订单号,之前生成的带用户ID的订单号
        String outTradeNo = new String(params.get("out_trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        //支付宝交易号
        String tradeNo = new String(params.get("trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        //付款金额
        String totalAmount = new String(params.get("total_amount").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        //交易状态
        String tradeStatus = new String(params.get("trade_status").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        /*
         * 交易状态
         * TRADE_SUCCESS 交易完成
         * TRADE_FINISHED 支付成功
         * WAIT_BUYER_PAY 交易创建
         * TRADE_CLOSED 交易关闭
         */
        log.info("tradeStatus:" + tradeStatus);

        if (tradeStatus.equals(TRADE_FINISHED)) {
            /*此处可自由发挥*/
            //判断该笔订单是否在商户网站中已经做过处理
            //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
            //如果有做过处理，不执行商户的业务程序
            //注意：
            //退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
        } else if (tradeStatus.equals(TRADE_SUCCESS)) {
            //判断该笔订单是否在商户网站中已经做过处理
            //如果没有做过处理，根据订单号（out_trade_no在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
            //如果有做过处理，不执行商户的业务程序

            // 此处代表交易已经成果，编写实际页面代码
            // 比如：重置成功，那么往数据库中写入重置金额
            log.info("trade_no:" + tradeNo);
            log.info("outTradeNo:" + outTradeNo);
            log.info("totalAmount:" + totalAmount);
        }

        out.print("success");
    }

    /**
     * 完成支付后的同步通知
     * 页面跳转同步通知页面路径，接收支付宝回调后，再封装页面数据，直接返回相应页面到前端
     * @param request
     * @param response
     * @throws IOException
     */
    @GetMapping("/return")
    public void returnUrl(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("同步通知");

        //乱码解决，这段代码在出现乱码时使用
        request.setCharacterEncoding("utf-8");

        //获取支付宝GET过来反馈信息
        Map<String, String> params = new HashMap<>(8);
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> stringEntry : requestParams.entrySet()) {
            String[] values = stringEntry.getValue();
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(stringEntry.getKey(), valueStr);
        }

        //商户订单号,之前生成的带用户ID的订单号
        String outTradeNo = new String(params.get("out_trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        //支付宝交易号
        String tradeNo = new String(params.get("trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        //付款金额
        String totalAmount = new String(params.get("total_amount").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        log.info("trade_no:" + tradeNo);
        log.info("outTradeNo:" + outTradeNo);
        log.info("totalAmount:" + totalAmount);

        //调用SDK验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, charset, signType);

        if (signVerified) {
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String redirectUrl = baseUrl + "/front/page/pay-success.html";
            log.info("验签成功 - 跳转到成功后页面: " + redirectUrl);
            response.sendRedirect(redirectUrl);
        } else {
            log.info("验签失败 - 跳转到充值页面让用户重新充值");
            response.sendRedirect("/path-to-recharge-page");
        }
    }

    /**
     * 用户下单
     * @param orders
     * @return 请求 URL: http://localhost:8080/order/submit
     * 负载：{remark: "多加辣椒！！！", payMethod: 1, addressBookId: "1580164651377868802"}
     */
    /*@PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }
*/
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders, HttpServletResponse httpResponse) throws IOException {

        try {
            // 提交订单和发起支付，返回支付页面表单HTML
            String payHtml = orderService.submit(orders);

            // 将支付页面表单HTML嵌入到R对象中，并返回
            return R.success(payHtml);

        } catch (AlipayApiException e) {
            log.error("支付宝支付失败", e);
            // 返回支付宝支付失败的信息
            return R.error("支付宝支付失败，请联系客服");
        }

    }


    /**
     * 后台查询订单明细
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime) {
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        //构造条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        //添加查询条件  动态sql  字符串使用StringUtils.isNotEmpty这个方法来判断
        //这里使用了范围查询的动态SQL，这里是重点！！！
        queryWrapper.like(number != null, Orders::getNumber, number)
                .gt(StringUtils.isNotEmpty(beginTime), Orders::getOrderTime, beginTime)
                .lt(StringUtils.isNotEmpty(endTime), Orders::getOrderTime, endTime);

        orderService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    //抽离的一个方法，通过订单id查询订单明细，得到一个订单明细的集合
    //这里抽离出来是为了避免在stream中遍历的时候直接使用构造条件来查询导致eq叠加，从而导致后面查询的数据都是null
    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);
        return orderDetailList;
    }

    /**
     * 移动端展示自己的订单分页查询
     * @param page
     * @param pageSize
     * @return 遇到的坑：原来分页对象中的records集合存储的对象是分页泛型中的对象，里面有分页泛型对象的数据
     * 开始的时候我以为前端只传过来了分页数据，其他所有的数据都要从本地线程存储的用户id开始查询，
     * 结果就出现了一个用户id查询到 n个订单对象，然后又使用 n个订单对象又去查询 m 个订单明细对象，
     * 结果就出现了评论区老哥出现的bug(嵌套显示数据....)
     * 正确方法:直接从分页对象中获取订单id就行，问题大大简化了......
     */
    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize) {
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrderDto> pageDto = new Page<>(page, pageSize);
        //构造条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        //这里是直接把当前用户分页的全部结果查询出来，要添加用户id作为查询条件，否则会出现用户可以查询到其他用户的订单情况
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo, queryWrapper);

        //通过OrderId查询对应的OrderDetail
        LambdaQueryWrapper<OrderDetail> queryWrapper2 = new LambdaQueryWrapper<>();

        //对OrderDto进行需要的属性赋值
        List<Orders> records = pageInfo.getRecords();
        List<OrderDto> orderDtoList = records.stream().map((item) -> {
            OrderDto orderDto = new OrderDto();
            //此时的orderDto对象里面orderDetails属性还是空 下面准备为它赋值
            Long orderId = item.getId();//获取订单id
            List<OrderDetail> orderDetailList = this.getOrderDetailListByOrderId(orderId);
            BeanUtils.copyProperties(item, orderDto);
            //对orderDto进行OrderDetails属性的赋值
            orderDto.setOrderDetails(orderDetailList);
            return orderDto;
        }).collect(Collectors.toList());

        //使用dto的分页有点难度.....需要重点掌握
        BeanUtils.copyProperties(pageInfo, pageDto, "records");
        pageDto.setRecords(orderDtoList);
        return R.success(pageDto);
    }

    /**
     * 订单派送，订单完成操作
     * 注：后端操作
     * @param orders
     * @return
     */
    @PutMapping
    public R<Orders> dispatch(@RequestBody Orders orders) {
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(orders.getId() != null, Orders::getId, orders.getId());
        Orders one = orderService.getOne(queryWrapper);

        one.setStatus(orders.getStatus());
        orderService.updateById(one);
        return R.success(one);
    }

    //移动端点击再来一单

    /**
     * 前端点击再来一单是直接跳转到购物车的，所以为了避免数据有问题，再跳转之前我们需要把购物车的数据给清除
     * ①通过orderId获取订单明细
     * ②把订单明细的数据的数据塞到购物车表中，不过在此之前要先把购物车表中的数据给清除(清除的是当前登录用户的购物车表中的数据)，
     * 不然就会导致再来一单的数据有问题；
     * (这样可能会影响用户体验，但是对于外卖来说，用户体验的影响不是很大，电商项目就不能这么干了)
     */
    @PostMapping("/again")
    public R<String> againSubmit(@RequestBody Map<String, String> map) {
        //获取再来一单的订单id
        String ids = map.get("id");
        long id = Long.parseLong(ids);

        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, id);
        //获取所有该订单中的菜品
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);

        //通过用户id把原来的购物车给清空，这里的clean方法是视频中讲过的,建议抽取到service中,那么这里就可以直接调用了
        shoppingCartService.clean();

        //获取用户id
        Long userId = BaseContext.getCurrentId();
        //因为菜品详细表和购物车表内容很像，所有容易相互赋值
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map((item) -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            shoppingCart.setImage(item.getImage());
            Long dishId = item.getDishId();
            Long setmealId = item.getSetmealId();
            if (dishId != null) {
                //如果是菜品那就添加菜品的查询条件
                shoppingCart.setDishId(dishId);
            } else {
                //添加到购物车的是套餐
                shoppingCart.setSetmealId(setmealId);
            }
            shoppingCart.setName(item.getName());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        //把携带数据的购物车批量插入购物车表  这个批量保存的方法要使用熟练！！！
        shoppingCartService.saveBatch(shoppingCartList);

        return R.success("操作成功");
    }
}