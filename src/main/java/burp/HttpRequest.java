package burp;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequest implements Runnable{
    private final IExtensionHelpers helpers;
    private final IBurpExtenderCallbacks callbacks;
    private final PrintWriter stdout;
    private final IHttpRequestResponse[] messages;
    private String jsonString;

    HttpRequest(IExtensionHelpers helpers, PrintWriter stdout, IBurpExtenderCallbacks callbacks, IHttpRequestResponse[] messages) {
        this.messages = messages;
        this.helpers = helpers;
        this.stdout = stdout;
        this.callbacks = callbacks;
    }
    @Override
    public void run() {
        byte[] request = this.messages[0].getRequest();
        IHttpService httpService = this.messages[0].getHttpService();
        IRequestInfo requestInfo = this.helpers.analyzeRequest(request);
        List<String> headers = requestInfo.getHeaders();
        String stringRequest = this.helpers.bytesToString(request);
        String pattern = "\\{\"([a-zA-Z_]+)\":(.+)}";
        Pattern p = Pattern.compile(pattern);

        for(Matcher m = p.matcher(stringRequest); m.find(); this.jsonString = m.group(0)) {
        }

        List<IParameter> iParameter = requestInfo.getParameters();
        Iterator iParameterList = iParameter.iterator();

        while(iParameterList.hasNext()) {
            IParameter iParameterName = (IParameter)iParameterList.next();
            final String parameterName;
            byte[] upDateParameter;
            if (iParameterName.getType() == 6) {
                parameterName = iParameterName.getName();
                JSONObject object = JSONObject.parseObject(this.jsonString);
                ValueFilter valueFilter = new ValueFilter() {
                    public Object process(Object object, String name, Object value) {
                        return parameterName.equals(name) ? null : value;
                    }
                };
                String jsonString = JSONObject.toJSONString(object, valueFilter, new SerializerFeature[0]);
                upDateParameter = this.helpers.stringToBytes(jsonString);
                byte[] newRequest = this.helpers.buildHttpMessage(headers, upDateParameter);

                try {
                    this.callbacks.makeHttpRequest(httpService, newRequest);
                } catch (Throwable error) {
                    this.stdout.println(error);
                    break;
                }
            } else {
                parameterName = iParameterName.getName();
                String parameterValue = "";
                byte parameterType = iParameterName.getType();
                IParameter newParameter = this.helpers.buildParameter(parameterName, parameterValue, parameterType);
                upDateParameter = this.helpers.updateParameter(request, newParameter);

                try {
                    this.callbacks.makeHttpRequest(httpService, upDateParameter);
                } catch (Throwable error) {
                    this.stdout.println(error);
                    break;
                }
            }
        }
    }
}
