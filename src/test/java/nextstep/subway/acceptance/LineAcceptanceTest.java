package nextstep.subway.acceptance;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.utils.DatabaseSetupTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nextstep.subway.acceptance.AcceptanceTestUtil.노선_생성_Extract;
import static nextstep.subway.acceptance.AcceptanceTestUtil.노선_조회_Extract;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class LineAcceptanceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @Transactional
    void setUp() {
        LineAndStationSetup lineAndStationSetup = new LineAndStationSetup(jdbcTemplate);
        lineAndStationSetup.setUpDatabase();
    }

    @Test
    @DisplayName("지하철 노선을 생성한다")
    void createLine() {
        // given
        Map<String, Object> params = getLineRequestParamMap("신분당선", "bg-red-600", 1L, 2L, 10L);

        // when
        ExtractableResponse<Response> response = 노선_생성_Extract(params);

        // then
        String name = response.jsonPath().getString("name");
        String color = response.jsonPath().getString("color");
        List<Map<String, Object>> stations = response.jsonPath().getList("stations");

        assertThat(name).isEqualTo("신분당선");
        assertThat(color).isEqualTo("bg-red-600");
        assertThat(stations).hasSize(2);
        assertThat(stations.get(0).get("id")).isEqualTo(1);
        assertThat(stations.get(0).get("name")).isEqualTo("강남역");
        assertThat(stations.get(1).get("id")).isEqualTo(2);
        assertThat(stations.get(1).get("name")).isEqualTo("판교역");
    }

    @Test
    @DisplayName("지하철 노선 목록을 조회한다.")
    void viewLineList() {
        // given
        Map<String, Object> params1 = getLineRequestParamMap("신분당선", "bg-red-600", 1L, 2L, 10L);

        노선_생성_Extract(params1);

        Map<String, Object> params2 = getLineRequestParamMap("분당선", "bg-green-600", 1L, 3L, 7L);

        노선_생성_Extract(params2);

        // when
        List<Map<String, Object>> response = getLineListExtract().jsonPath().getList("$");

        // then
        assertThat(response).hasSize(2);

        assertThat(response.get(0).get("name")).isEqualTo("신분당선");
        assertThat(response.get(0).get("color")).isEqualTo("bg-red-600");
        List<Map<String, Object>> stations1 = (List<Map<String, Object>>) response.get(0).get("stations");
        assertThat(stations1.size()).isEqualTo(2);
        assertThat(stations1.get(0).get("id")).isEqualTo(1);
        assertThat(stations1.get(1).get("id")).isEqualTo(2);

        assertThat(response.get(1).get("name")).isEqualTo("분당선");
        assertThat(response.get(1).get("color")).isEqualTo("bg-green-600");
        List<Map<String, Object>> stations2 = (List<Map<String, Object>>) response.get(1).get("stations");
        assertThat(stations2.size()).isEqualTo(2);
        assertThat(stations2.get(0).get("id")).isEqualTo(1);
        assertThat(stations2.get(1).get("id")).isEqualTo(3);
    }

    @Test
    @DisplayName("지하철 노선을 조회한다.")
    void viewLine() {
        // given
        Map<String, Object> params1 = getLineRequestParamMap("신분당선", "bg-red-600", 1L, 2L, 10L);

        long lineId = 노선_생성_Extract(params1).jsonPath().getLong("id");

        // when
        ExtractableResponse<Response> response = 노선_조회_Extract(lineId);

        // then
        Long responseLineId = response.jsonPath().getLong("id");
        String responseLineName = response.jsonPath().getString("name");
        String responseLineColor = response.jsonPath().getString("color");
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> stations = response.jsonPath().getList("stations");

        assertThat(responseLineId).isEqualTo(lineId);
        assertThat(responseLineName).isEqualTo("신분당선");
        assertThat(responseLineColor).isEqualTo("bg-red-600");
        assertThat(stations).hasSize(2);
        assertThat(stations.get(0).get("id")).isEqualTo(1);
        assertThat(stations.get(1).get("id")).isEqualTo(2);
    }


    @Test
    @DisplayName("지하철 노선을 수정한다.")
    void updateLine() {
        // given
        Map<String, Object> params1 = getLineRequestParamMap("신분당선", "bg-red-600", 1L, 2L, 10L);

        long lineId = 노선_생성_Extract(params1).jsonPath().getLong("id");

        // when
        Map<String, Object> putParams = new HashMap<>();
        putParams.put("name", "다른분당선");
        putParams.put("color", "bg-red-700");

        ExtractableResponse<Response> patchResponse = patchLineExtract(putParams, lineId);

        ExtractableResponse<Response> viewResponse = 노선_조회_Extract(lineId);

        // then
        assertThat(patchResponse.statusCode()).isEqualTo(200);

        assertThat(viewResponse.jsonPath().getString("name")).isEqualTo("다른분당선");
        assertThat(viewResponse.jsonPath().getString("color")).isEqualTo("bg-red-700");
    }

    @Test
    @DisplayName("지하철 노선을 삭제한다.")
    void DeleteLine() {
        // given
        Map<String, Object> params1 = getLineRequestParamMap("신분당선", "bg-red-600", 1L, 2L, 10L);

        long lineId = 노선_생성_Extract(params1).jsonPath().getLong("id");
        // when
        ExtractableResponse<Response> deleteResponse = deleteLineExtract(lineId);

        // then
        assertThat(deleteResponse.statusCode()).isEqualTo(204);
        assertThat(노선_조회_Extract(lineId).statusCode()).isEqualTo(404);
    }

    private Map<String, Object> getLineRequestParamMap(
            String name,
            String color,
            Long upStationId,
            Long downStationId,
            Long distance) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("color", color);
        params.put("upStationId", upStationId);
        params.put("downStationId", downStationId);
        params.put("distance", distance);
        return params;
    }
    private ExtractableResponse<Response> getLineListExtract() {
        return RestAssured.given().log().all()
                .when().get("/lines")
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> deleteLineExtract(long lineId) {
        return RestAssured.given().log().all()
                .when().delete("/lines/" + lineId)
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> patchLineExtract(Map<String, Object> putParams, long lineId) {
        return RestAssured.given().log().all()
                .body(putParams)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().patch("/lines/" + lineId)
                .then().log().all()
                .extract();
    }

    private class LineAndStationSetup extends DatabaseSetupTemplate {

        public LineAndStationSetup(JdbcTemplate jdbcTemplate) {
            super(jdbcTemplate);
        }

        @Override
        protected void truncateTables() {
            jdbcTemplate.execute("TRUNCATE TABLE line");
            jdbcTemplate.execute("TRUNCATE TABLE station");
        }

        @Override
        protected void resetAutoIncrement() {
            jdbcTemplate.execute("ALTER TABLE station ALTER COLUMN id RESTART WITH 1");
            jdbcTemplate.execute("ALTER TABLE line ALTER COLUMN id RESTART WITH 1");
        }

        @Override
        protected void insertInitialData() {
            jdbcTemplate.update("INSERT INTO station (name) VALUES ('강남역')");
            jdbcTemplate.update("INSERT INTO station (name) VALUES ('판교역')");
            jdbcTemplate.update("INSERT INTO station (name) VALUES ('광교역')");
        }
    }
}
