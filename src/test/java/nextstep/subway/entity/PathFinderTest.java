package nextstep.subway.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PathFinderTest {
    @Test
    @DisplayName("최단경로 조회")
    void 최단거리_조회() {
        // given
        Station 교대역 = new Station("교대역");
        Station 강남역 = new Station("강남역");
        Station 양재역 = new Station("양재역");
        Station 남부터미널역 = new Station("남부터미널역");

        Line 이호선 = new Line("2호선", "bg-green-600");
        이호선.addSection(교대역, 강남역, 10L);

        Line 신분당선 = new Line("신분당선", "bg-blue-600");
        신분당선.addSection(강남역, 양재역, 10L);


        Line 삼호선 = new Line("3호선", "bg-red-600");
        삼호선.addSection(교대역, 남부터미널역, 2L);
        삼호선.addSection(남부터미널역, 양재역, 3L);

        PathFinder pathFinder = new PathFinder();
        pathFinder.addLine(이호선);
        pathFinder.addLine(신분당선);
        pathFinder.addLine(삼호선);

        // when
        Path path = pathFinder.getPath(교대역, 양재역);

        // then
        assertThat(path.getStations()).hasSize(3);
        assertThat(path.getStations()).containsExactly(교대역, 남부터미널역, 양재역);
        assertThat(path.getDistance()).isEqualTo(5L);
    }
}
