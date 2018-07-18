import com.Application;
import com.alibaba.fastjson.JSONObject;
import com.zoudong.permission.constant.JwtConstant;
import com.zoudong.permission.utils.EhcacheUtil;
import com.zoudong.permission.utils.RedisUtils;
import com.zoudong.permission.utils.jwt.JwtUtil;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.bind.DatatypeConverter;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author zd
 * @description class
 * @date 2018/6/4 17:10
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class JwtTokenTest {
    @Autowired
   JwtUtil jwtUtil;
    @Test
    public void t()throws Exception{
        jwtUtil.generalKey();

        JSONObject jo = new JSONObject();
        jo.put("userId", "zoudong");
        jo.put("mobile", "1234567");

        log.info(jwtUtil.createJWT(jwtUtil.generalKey().toString(),jo.toJSONString(), JwtConstant.JWT_TTL));



    }


}
