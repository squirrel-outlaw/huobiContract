package com.huobi.api;

import com.huobi.constant.HuobiConsts;
import com.huobi.domain.response.RespBody;
import com.huobi.exception.HuobiApiException;
import com.huobi.utils.security.AuthenticationInterceptor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;

import static com.huobi.utils.PrintUtil.print;

/**
 * created by jacky. 2018/7/20 9:07 PM
 */
@Slf4j
public class HuobiApiServiceGenerator {

    static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(HuobiConsts.API_URL)
                    .addConverterFactory(JacksonConverterFactory.create());


    private static Retrofit retrofit = builder.build();

    public static <S> S createService(Class<S> serviceClass) {
        return createService(serviceClass, null, null);
    }

    public static <S> S createService(Class<S> serviceClass, String apiKey, String secret) {
        if (!StringUtils.isEmpty(apiKey) && !StringUtils.isEmpty(secret)) {
            AuthenticationInterceptor interceptor = new AuthenticationInterceptor(apiKey, secret);
            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(interceptor);
                builder.client(httpClient.build());
                retrofit = builder.build();
            }
        }
        return retrofit.create(serviceClass);
    }

    /**
     * Execute a REST call and block until the response is received.
     */
    public static <T> T executeSync(Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                parseBody(response.body());
                return response.body();
            } else {
                parseError(response);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (HuobiApiException e) {
            log.error("errMsg:"+e.getMessage()+",errCode:"+e.getErrCode());
        }
        //此异常必须被捕获，否则程序终止运行
        throw new IllegalStateException("invalid response from server.");
    }

    private static <T> void parseBody(T body) throws HuobiApiException {
        if (body instanceof RespBody) {
            RespBody resp = (RespBody) body;
            if (!resp.getStatus().equalsIgnoreCase("ok")) {
                throw new HuobiApiException(resp.toErrorString());
            }
        }
    }

    /**
     * Extracts and converts the response error body into an object.
     */
    public static HuobiApiException parseError(Response<?> response) throws HuobiApiException {
        throw new HuobiApiException(response.raw().code(), response.raw().message());
    }


}
