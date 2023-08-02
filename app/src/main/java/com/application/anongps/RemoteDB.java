package com.application.anongps;

import org.json.JSONException;
import org.json.JSONObject;

public class RemoteDB {
    private String deviceUuid;
    private final String baseUrl = "https://anongps-d19af-default-rtdb.europe-west1.firebasedatabase.app/devices/";
    private OkHttpHandler request = new OkHttpHandler();
    private String JSONData;

    public RemoteDB(String deviceUuid){
        this.deviceUuid = deviceUuid;
    }


    public void saveData(String lat, String lon, String alt, String speed, String time) {
        JSONData =  "{\"uuid\": {\"lat\": \"enclat\", \"lon\": \"enclon\", \"alt\": \"encalt\", \"speed\": \"encspeed\", \"time\": \"unixtime\"}}"
                .replace("uuid", deviceUuid)
                .replace("enclat", lat)
                .replace("enclon", lon)
                .replace("encalt", alt)
                .replace("encspeed", speed)
                .replace("unixtime", time);
        request.patchRequest(baseUrl + ".json", JSONData);
    }

    public void deleteData(String uuid){
        request.deleteRequest(baseUrl + uuid +".json");
    }

    public Device getData(){
        Device dev;
        JSONData = request.getRequest(baseUrl + deviceUuid + ".json");
        JSONObject json = null;
        try {
            json = new JSONObject(JSONData);
            dev = new Device(deviceUuid);

            dev.setAlt(json.getString("alt"));
            dev.setLat(json.getString("lat"));
            dev.setLon(json.getString("lon"));
            dev.setSpeed(json.getString("speed"));
            dev.setTime(json.getString("time"));
        } catch (JSONException e) {
            //this means json is null, no device found
            dev = null;
        }
        return dev;
    }
}
