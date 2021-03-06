package com.authy.api;

import com.authy.AuthyException;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Julian Camargo
 */
public class Users extends Resource {
    public static final String NEW_USER_PATH = "/protected/json/users/new";
    public static final String DELETE_USER_PATH = "/protected/json/users/delete/";
    public static final String SMS_PATH = "/protected/json/sms/";
    public static final String ONE_CODE_CALL_PATH = "/protected/json/call/";
    public static final String DEFAULT_COUNTRY_CODE = "1";

    public Users(String uri, String key) {
        super(uri, key, Resource.JSON_CONTENT_TYPE);
    }

    public Users(String uri, String key, boolean testFlag) {
        super(uri, key, testFlag, Resource.JSON_CONTENT_TYPE);
    }

    /**
     * Create a new user using his e-mail, phone and country code.
     *
     * @param email
     * @param phone
     * @param countryCode
     * @return a User instance
     */
    public com.authy.api.User createUser(String email, String phone, String countryCode) throws AuthyException {
        User user = new User(email, phone, countryCode);

        String content = this.post(NEW_USER_PATH, user);

        return userFromJson(this.getStatus(), content);
    }

    /**
     * Create a new user using his e-mail and phone. It uses USA country code by default.
     *
     * @param email
     * @param phone
     * @return a User instance
     */
    public com.authy.api.User createUser(String email, String phone) throws AuthyException {
        return createUser(email, phone, DEFAULT_COUNTRY_CODE);
    }

    /**
     * Send token via sms to a user.
     *
     * @param userId
     * @return Hash instance with API's response.
     */
    public Hash requestSms(int userId) throws AuthyException {
        return requestSms(userId, new HashMap<>(0));
    }

    /**
     * Send token via sms to a user with some options defined.
     *
     * @param userId
     * @param options
     * @return Hash instance with API's response.
     */
    public Hash requestSms(int userId, Map<String, String> options) throws AuthyException {
        MapToResponse opt = new MapToResponse(options);
        String content = this.get(SMS_PATH + Integer.toString(userId), opt);
        return instanceFromJson(this.getStatus(), content);
    }

    /**
     * Send token via call to a user.
     *
     * @param userId
     * @return Hash instance with API's response.
     */
    public Hash requestCall(int userId) throws AuthyException {
        return requestCall(userId, new HashMap<>(0));
    }

    /**
     * Send token via call to a user with some options defined.
     *
     * @param userId
     * @param options
     * @return Hash instance with API's response.
     */
    public Hash requestCall(int userId, Map<String, String> options) throws AuthyException {
        MapToResponse opt = new MapToResponse(options);
        String content = this.get(ONE_CODE_CALL_PATH + Integer.toString(userId), opt);
        return instanceFromJson(this.getStatus(), content);
    }

    /**
     * Delete a user.
     *
     * @param userId
     * @return Hash instance with API's response.
     */
    public Hash deleteUser(int userId) throws AuthyException {
        String content = this.post(DELETE_USER_PATH + Integer.toString(userId), null);
        return instanceFromJson(this.getStatus(), content);
    }

    private com.authy.api.User userFromJson(int status, String content) throws AuthyException {
        com.authy.api.User user = new com.authy.api.User();
        user.setStatus(status);
        if (user.isOk()) {
            JSONObject userJson = new JSONObject(content);
            user.setId(userJson.getJSONObject("user").getInt("id"));
        } else {
            Error error = errorFromJson(status, content);
            user.setError(error);
        }
        return user;
    }

    private Hash instanceFromJson(int status, String content) throws AuthyException {
        Hash hash = new Hash();
        hash.setStatus(status);
        if (hash.isOk()) {
            try {
                JSONObject jsonResponse = new JSONObject(content);
                String message = jsonResponse.optString("message");
                hash.setMessage(message);

                boolean success = jsonResponse.optBoolean("success");
                hash.setSuccess(success);

                String token = jsonResponse.optString("token");
                hash.setToken(token);
            } catch (JSONException e) {
                throw new AuthyException("Invalid response from server", e);
            }
        } else {
            Error error = errorFromJson(status, content);
            hash.setError(error);
        }

        return hash;
    }

    static class MapToResponse implements Formattable {
        private Map<String, String> options;

        public MapToResponse(Map<String, String> options) {
            this.options = options;
        }

        public String toXML() {
            return "";
        }

        public Map<String, String> toMap() {
            return options;
        }
    }

    @XmlRootElement(name = "user")
    static class User implements Formattable {
        String email, cellphone, countryCode;

        public User() {
        }

        public User(String email, String cellphone, String countryCode) {
            this.email = email;
            this.cellphone = cellphone;
            this.countryCode = countryCode;
        }

        @XmlElement(name = "email")
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @XmlElement(name = "cellphone")
        public String getCellphone() {
            return cellphone;
        }

        public void setCellphone(String cellphone) {
            this.cellphone = cellphone;
        }

        @XmlElement(name = "country_code")
        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String toXML() {
            StringWriter sw = new StringWriter();
            String xml = "";

            try {
                JAXBContext context = JAXBContext.newInstance(this.getClass());
                Marshaller marshaller = context.createMarshaller();

                marshaller.marshal(this, sw);

                xml = sw.toString();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
            return xml;
        }

        public Map<String, String> toMap() {

            Map<String, String> map = new HashMap<>();
            map.put("email", email);
            map.put("cellphone", cellphone);
            map.put("country_code", countryCode);

            return map;
        }

        @Override
        public String toJSON() {
            JSONObject json = new JSONObject();
            json.put("user", toMap());
            return json.toString();
        }
    }
}
