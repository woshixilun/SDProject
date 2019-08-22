package app.controller;

import app.exception.IllegalParameterException;
import app.service.LogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static app.service.DatabaseService.getResultSet;

/**
 * Created by yubzhu on 19-8-4
 */

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/geometry")
public class GeometryController {

    @Autowired
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final LogService log = new LogService(GeometryController.class);

    @GetMapping("/default")
    public ObjectNode queryDefaultDistrictPolygon(HttpServletRequest httpServletRequest) {
        return queryDistrictPolygon(httpServletRequest, null, null, null, "city");
    }

    private ArrayNode multiPolygonStringToArrayNode(String multiPolygonString) throws IOException{
        return (ArrayNode)objectMapper.readTree(multiPolygonString.replace("MULTIPOLYGON(((", "[[[").replace(")))", "]]]").replace(")),((", "],[").replace(",", "],[").replace(" ", ", ").replace("],[", "], ["));
    }

    private ArrayNode packupPolygon(String sqlSentence, String level) throws InterruptedException, ExecutionException, TimeoutException, SQLException, IOException {
        ResultSet resultSet = getResultSet(sqlSentence);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        while (resultSet.next()) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            if (level.equals("country")) {
                objectNode.put("name", "中华人民共和国");
            } else {
                objectNode.put("name", resultSet.getString(level));
            }
            objectNode.set("multipolygon", multiPolygonStringToArrayNode(resultSet.getString("st_astext")));
            arrayNode.add(objectNode);
        }
        return arrayNode;
    }

    @GetMapping("/district")
    public ObjectNode queryDistrictPolygon(HttpServletRequest httpServletRequest,
                                           @RequestParam(required = false, value = "province") String province,
                                           @RequestParam(required = false, value = "city") String city,
                                           @RequestParam(required = false, value = "district") String district,
                                           @RequestParam(required = false, value = "level") String level) {
        try {
            ObjectNode objectNode = objectMapper.createObjectNode();
            if (level.equals("country") || level.equals("province") || level.equals("city") || level.equals("district")) {
                if (province == null) {
                    if (city != null || district != null) {
                        throw new IllegalParameterException();
                    }
                    objectNode.set("country", packupPolygon("select st_astext(geom) from district_boundary where province = '全部'", "country"));
                }
            } else {
                throw new IllegalParameterException();
            }
            if (level.equals("province") || level.equals("city") || level.equals("district")) {
                if (province == null) {
                    objectNode.set("province", packupPolygon("select province, st_astext(geom) from district_boundary where province != '全部' and city = '全部'", "province"));
                } else if (city == null) {
                    if (district != null) {
                        throw new IllegalParameterException();
                    }
                    objectNode.set("province", packupPolygon("select province, st_astext(geom) from district_boundary where province = '" + province + "' and city = '全部'", "province"));
                }
            }
            if (level.equals("city") || level.equals("district")) {
                if (city == null) {
                    objectNode.set("city", packupPolygon("select city, st_astext(geom) from district_boundary where city != '全部' and district = '全部'", "city"));
                } else if (district == null) {
                    objectNode.set("city", packupPolygon("select city, st_astext(geom) from district_boundary where city = '" + city + "' and district = '全部'", "city"));
                }
            }
            if (level.equals("district")) {
                if (district == null) {
                    objectNode.set("district", packupPolygon("select district, st_astext(geom) from district_boundary where district != '全部'", "district"));
                } else {
                    objectNode.set("district", packupPolygon("select district, st_astext(geom) from district_boundary where district = '" + district + "'", "district"));
                }
            }
            log.printExecuteOkInfo(httpServletRequest);
            return objectNode;
        } catch (InterruptedException | ExecutionException | TimeoutException | SQLException | NullPointerException | IOException | IllegalParameterException e) {
            log.printExceptionOccurredError(httpServletRequest, e);
            return objectMapper.createObjectNode().put("exception", e.getClass().getSimpleName());
        }
    }

    @GetMapping("/relation")
    public ObjectNode queryRelation(HttpServletRequest httpServletRequest) {
        //todo
        return null;
    }

    @GetMapping("/customize")
    public ObjectNode updateCustomizedRegion(HttpServletRequest httpServletRequest) {
        // todo
        return null;
    }

}
