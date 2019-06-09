package app.controller;

import app.service.LogService;
import app.service.QueryTableService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by yubzhu on 2019/6/9
 */

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/heatmap")
public class HeatmapController {

    @Autowired
    private static ObjectMapper objectMapper = new ObjectMapper();

    private static LogService log = new LogService(HeatmapController.class);

    @GetMapping("/qingdao")
    public ObjectNode queryCompanyHeatmap(HttpServletRequest httpServletRequest) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        try {
            String sqlSentence = "select lon, lat from ent_info";
            ResultSet resultSet = QueryTableService.query(sqlSentence);
            int heatmapPrecision = 4;
            HashMap<String, Integer> hashMap = new HashMap<>();
            while (resultSet.next()) {
                String lon = String.valueOf(resultSet.getDouble("lon"));
                String lat = String.valueOf(resultSet.getDouble("lat"));
                String index = lon.split("\\.")[0] + "." + lon.split("\\.")[1].concat("0000").substring(0, heatmapPrecision) + "," + lat.split("\\.")[0] + "." + lat.split("\\.")[1].concat("0000").substring(0, heatmapPrecision);
                hashMap.putIfAbsent(index, 0);
                hashMap.put(index, hashMap.get(index) + 1);
            }
            objectNode.put("type", "FeatureCollection");
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (String string : hashMap.keySet()) {
                ObjectNode tempObjectNode = objectMapper.createObjectNode();
                tempObjectNode.put("type", "Feature");
                tempObjectNode.put("weight", hashMap.get(string));
                ObjectNode insideObjectNode = objectMapper.createObjectNode();
                insideObjectNode.put("type", "Point");
                ArrayNode insideArrayNode = objectMapper.createArrayNode();
                insideArrayNode.add(Double.parseDouble(string.split(",")[0]));
                insideArrayNode.add(Double.parseDouble(string.split(",")[1]));
                insideObjectNode.set("coordinates", insideArrayNode);
                tempObjectNode.set("geometry", insideObjectNode);
                arrayNode.add(tempObjectNode);
            }
            objectNode.set("features", arrayNode);
            log.printQueryOkInfo(httpServletRequest);
        } catch (ClassNotFoundException | SQLException e) {
            log.printExceptionOccurredWarning(httpServletRequest, e);
            objectNode.removeAll();
            objectNode.put("exception", e.getClass().getSimpleName());
        }
        return objectNode;
    }

}
