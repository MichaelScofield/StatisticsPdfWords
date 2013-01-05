package me.lfc;

/**
 * User: luofucong
 * Date: 13-1-1
 */
public class BingAccessToken {

    private String access_token;

    private String token_type;

    private String expires_in;

    private String scope;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(String expires_in) {
        this.expires_in = expires_in;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getHeadValue() {
        return "Bearer " + access_token;
    }

    @Override
    public String toString() {
        return "BingAccessToken {" + "\n" +
                "  access_token=\"" + access_token + "\"" + "\n" +
                "  token_type=\"" + token_type + "\"" + "\n" +
                "  expires_in=\"" + expires_in + "\"" + "\n" +
                "  scope=\"" + scope + "\"" + "\n" +
                '}';
    }
}
