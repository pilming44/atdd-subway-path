package nextstep.subway.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import nextstep.subway.entity.Section;

public interface SectionRepository extends JpaRepository<Section, Long> {
}
