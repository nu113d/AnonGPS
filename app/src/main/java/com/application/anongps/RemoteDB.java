package com.application.anongps;

public class RemoteDB {
    private String deviceUuid;
    private final String baseUrl = "https://anongps-d19af-default-rtdb.europe-west1.firebasedatabase.app/devices/";
    private OkHttpHandler httpRequest = new OkHttpHandler();
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
        httpRequest.makePatchRequest(baseUrl + ".json", JSONData);
    }

    public void deleteData(String uuid){
        httpRequest.deleteRequest(baseUrl + uuid +".json");
    }
}
