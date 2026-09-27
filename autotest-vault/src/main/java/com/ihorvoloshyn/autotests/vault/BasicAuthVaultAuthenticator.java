package com.ihorvoloshyn.autotests.vault;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihorvoloshyn.autotests.core.endpoint.EndpointResolver;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class BasicAuthVaultAuthenticator implements VaultAuthenticator {
    private final HttpClient httpClient=HttpClient.newHttpClient();
    private final ObjectMapper objectMapper=new ObjectMapper();
    private final String baseUrl,authPath,username,password;
    public BasicAuthVaultAuthenticator(String baseUrl,String authPath,String username,String password){
        this.baseUrl=EndpointResolver.resolve(baseUrl).toUri().toString().replaceAll("/+$","");
        this.authPath=trim(require(authPath,"authPath")); this.username=require(username,"username"); this.password=password==null?"":password;
    }
    public VaultToken authenticate(){
        String credentials=Base64.getEncoder().encodeToString((username+":"+password).getBytes(StandardCharsets.UTF_8));
        HttpRequest request=HttpRequest.newBuilder(URI.create(baseUrl+"/v1/"+authPath)).header("Authorization","Basic "+credentials).header("Accept","application/json").POST(HttpRequest.BodyPublishers.noBody()).build();
        try{
            HttpResponse<String> response=httpClient.send(request,HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()/100!=2) throw new IllegalStateException("Vault authentication failed: HTTP "+response.statusCode());
            JsonNode token=objectMapper.readTree(response.body()).path("auth").path("client_token");
            if(token.isMissingNode()||token.asText().isBlank()) throw new IllegalStateException("Vault authentication response does not contain auth.client_token");
            return new VaultToken(token.asText());
        }catch(IOException e){throw new IllegalStateException("Cannot authenticate against Vault",e);}
        catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("Vault authentication interrupted",e);}
    }
    private static String require(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" must not be blank");return v;}
    private static String trim(String v){return v.replaceAll("^/+|/+$","");}
}