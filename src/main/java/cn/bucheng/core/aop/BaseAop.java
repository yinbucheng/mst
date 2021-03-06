package cn.bucheng.core.aop;

import cn.bucheng.core.holder.ClientChannelHolder;
import cn.bucheng.core.holder.RequestResultHolder;
import cn.bucheng.model.req.TXRequest;
import cn.bucheng.model.res.TXResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ：yinchong
 * @create ：2019/7/11 17:01
 * @description：抽取重复的方法
 * @modified By：
 * @version:
 */
public class BaseAop {

    private static Logger logger = LoggerFactory.getLogger(BaseAop.class);

    //通知协调器并等待协调器回复
    public void notifyServer(String token, TXRequest request) {
        ClientChannelHolder.writeAndFlush(request);
        TXResponse response = RequestResultHolder.waitResult(token);
        if (null == response) {
            logger.error("in appoint time not accept server response");
            throw new RuntimeException("in appoint time not accept server response");
        }
    }
}
