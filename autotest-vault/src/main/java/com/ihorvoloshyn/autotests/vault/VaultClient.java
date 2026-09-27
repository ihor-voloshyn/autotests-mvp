package com.ihorvoloshyn.autotests.vault;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihorvoloshyn.autotests.core.endpoint.ConnectionEndpoint;
import com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.Map;

public class VaultClient {
    private final HttpClient httpClient=HttpClient.newHttpClient();
    private final ObjectMapper objectMapper=new ObjectMapper();
    private final ConnectionEndpoint endpoint;
    private final VaultToken token;

    public VaultClient(String baseUrl, VaultToken token) { this(EndpointResolver.resolve(baseUrl), token); }
    public VaultClient(ConnectionEndpoint endpoint, VaultToken token) {
        if(endpoint==null || endpoint.scheme()==null) throw new IllegalArgumentException("Vault endpoint must include a URL scheme");
        if(token==null) throw new IllegalArgumentException("token must not be null");
        this.endpoint=endpoint; this.token=token;
    }
    public VaultClient(String baseUrl,String token){this(baseUrl,new VaultToken(token));}

    public Map<String,Object> read(String path){return get(path);}
    public Map<String,Object> readData(String path){
        return extractMap(read(path).get("data"),"Vault response");
    }
    public Map<String,Object> readData(String mount,String path,VaultKvVersion version){
        if(mount==null||mount.isBlank()) throw new IllegalArgumentException("mount must not be blank");
        if(path==null||path.isBlank()) throw new IllegalArgumentException("path must not be blank");
        String p=switch(version){case KV1->trim(mount)+"/"+trim(path);case KV2->trim(mount)+"/data/"+trim(path);};
        Map<String,Object> response=get(p);
        if(version==VaultKvVersion.KV1) return extractMap(response.get("data"),"Vault KV1 response");
        return extractMap(extractMap(response.get("data"),"Vault KV2 response").get("data"),"Vault KV2 response data");
    }
    private Map<String,Object> get(String path){
        try{
            URI uri=URI.create(endpoint.toUri().toString().replaceAll("/+$","")+"/v1/"+trimLeading(path));
            HttpRequest request=HttpRequest.newBuilder(uri).header("X-Vault-Token",token.value()).header("Accept","application/json").GET().build();
            HttpResponse<String> response=httpClient.send(request,HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()/100!=2) throw new IllegalStateException("Vault request failed: HTTP "+response.statusCode());
            return objectMapper.readValue(response.body(),new TypeReference<Map<String,Object>>(){});
        }catch(IOException e){throw new IllegalStateException("Cannot read Vault response",e);}
        catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("Vault request interrupted",e);}
    }
    @SuppressWarnings("unchecked") private static Map<String,Object> extractMap(Object value,String message){
        if(!(value instanceof Map<?,?>)) throw new IllegalStateException(message+" does not contain object data");
        return (Map<String,Object>)value;
    }
    private static String trim(String v){return v.replaceAll("^/+|/+$","");}
    private static String trimLeading(String v){return v.replaceAll("^/+","");}
}