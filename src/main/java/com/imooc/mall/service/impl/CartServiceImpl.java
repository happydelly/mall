package com.imooc.mall.service.impl;

import com.google.gson.Gson;
import com.imooc.mall.dao.ProductMapper;
import com.imooc.mall.enums.ProductStatusEnum;
import com.imooc.mall.enums.ResponseEnum;
import com.imooc.mall.form.CartAddForm;
import com.imooc.mall.form.CartUpdateForm;
import com.imooc.mall.pojo.Cart;
import com.imooc.mall.pojo.Product;
import com.imooc.mall.service.ICartService;
import com.imooc.mall.utils.RedisOperator;
import com.imooc.mall.vo.CartProductVo;
import com.imooc.mall.vo.CartVo;
import com.imooc.mall.vo.ResponseVo;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.xml.ws.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements ICartService {

    private final static String CART_REDIS_KEY_TEMPLATE = "cart_%d";

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisOperator redisOperator;

    private Gson gson = new Gson();
    @Override
    public ResponseVo<CartVo> add(Integer uid,CartAddForm form) {
        Integer quantity = 1;
        Product product = productMapper.selectByPrimaryKey(form.getProductId());
        //判断商品是否存在
        if(product == null){
            return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXIST);
        }
        //商品是否正常在售
        if(!product.getStatus().equals(ProductStatusEnum.ON_SALE.getCode())){
            return ResponseVo.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE);
        }
        //商品库存是否充足
        if(product.getStock() <= 0){
            return ResponseVo.error(ResponseEnum.PROODUCT_STOCK_ERROR);
        }

        //写入rds
//        redisOperator.set(String.format(CART_REDIS_KEY_TEMPLATE, uid),
//                gson.toJson(new Cart(product.getId(),quantity,form.getSelected())));

        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);

        String value = redisOperator.hget(redisKey, String.valueOf(product.getId()));

        Cart cart;
        if(StringUtils.isEmpty(value)){
            //没有该商品,新增
            cart = new Cart(product.getId(), quantity, form.getSelected());
        }else{
            //已经有了,数量加1
            cart = gson.fromJson(value, Cart.class);
            cart.setQuantity(cart.getQuantity() + 1);
        }

        redisOperator.hset(String.format(CART_REDIS_KEY_TEMPLATE, uid),
                String.valueOf(product.getId()),
                gson.toJson(cart));

        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> list(Integer uid) {
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);

        HashOperations<String, String,String> opsForHash = redisOperator.hgetallstr();
        Map<String, String> entries = opsForHash.entries(redisKey);

        boolean selectAll = true;
        Integer cartTotalQuantity = 0;
        BigDecimal cartTotalPrice = BigDecimal.ZERO;
        CartVo cartVo = new CartVo();
        List<CartProductVo> cartProductVoList = new ArrayList<>();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            Integer productId = Integer.valueOf(entry.getKey());
            Cart cart = gson.fromJson(entry.getValue(),Cart.class);

            //TODO 需要优化,使用mysql里的in
            Product product = productMapper.selectByPrimaryKey(productId);
            if(product != null){
                CartProductVo cartProductVo = new CartProductVo(productId,
                        cart.getQuantity(),
                        product.getName(),
                        product.getSubtitle(),
                        product.getMainImage(),
                        product.getPrice(),
                        product.getStatus(),
                        product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())),
                        product.getStock(),
                        cart.getProductSelected());
                cartProductVoList.add(cartProductVo);
                if(!cart.getProductSelected()){
                    selectAll = false;
                }
                //计算总价(只计算选中的)
                if(cart.getProductSelected()){
                    cartTotalPrice = cartTotalPrice.add(cartProductVo.getProductTotalPrice());
                }

            }
            cartTotalQuantity += cart.getQuantity();
        }
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartTotalQuantity(cartTotalQuantity);
        cartVo.setSelectedAll(selectAll);

        return ResponseVo.success(cartVo);
    }

    @Override
    public ResponseVo<CartVo> update(Integer uid, Integer productId, CartUpdateForm form) {
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);

        String value = redisOperator.hget(redisKey, String.valueOf(productId));

        Cart cart;
        if(StringUtils.isEmpty(value)){
            //没有该商品,报错
            return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }

        //已经有了,修改内容
        cart = gson.fromJson(value, Cart.class);
        if(form.getQuantity() !=null && form.getQuantity() >=0){
            cart.setQuantity(form.getQuantity());
        }
        if(form.getSelected() !=null){
            cart.setProductSelected(form.getSelected());
        }
        redisOperator.hset(redisKey,String.valueOf(productId),gson.toJson(cart));

        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> delete(Integer uid, Integer productId) {

        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);

        String value = redisOperator.hget(redisKey, String.valueOf(productId));

        Cart cart;
        if(StringUtils.isEmpty(value)){
            //没有该商品,报错
            return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }

        redisOperator.hdel(redisKey,String.valueOf(productId));

        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> selectAll(Integer uid) {
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        HashOperations<String, String,String> opsForHash = redisOperator.hgetallstr();

        for (Cart cart : listForCart(uid)) {
            cart.setProductSelected(true);
            opsForHash.put(redisKey,
                    String.valueOf(cart.getProductId()),
                    gson.toJson(cart));
        }

        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> unSelectAll(Integer uid) {
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        HashOperations<String, String,String> opsForHash = redisOperator.hgetallstr();

        for (Cart cart : listForCart(uid)) {
            cart.setProductSelected(false);
            opsForHash.put(redisKey,
                    String.valueOf(cart.getProductId()),
                    gson.toJson(cart));
        }

        return list(uid);
    }

    @Override
    public ResponseVo<Integer> sum(Integer uid) {
        Integer sum = listForCart(uid).stream()
                .map(Cart::getQuantity)
                .reduce(0, Integer::sum);
        return ResponseVo.success(sum);
    }

    public List<Cart> listForCart(Integer uid) {
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        HashOperations<String, String,String> opsForHash = redisOperator.hgetallstr();
        Map<String, String> entries = opsForHash.entries(redisKey);

        List<Cart> cartList = new ArrayList<>();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            cartList.add(gson.fromJson(entry.getValue(), Cart.class));
        }

        return cartList;
    }

}
