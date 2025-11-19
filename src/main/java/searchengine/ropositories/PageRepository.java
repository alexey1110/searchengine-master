package searchengine.ropositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    List<Page> findBySiteId(Long siteId);

    List<Page> findByPathAndSiteId(String path, long siteId);

    void deleteAllBySiteId(Long siteId);
}
