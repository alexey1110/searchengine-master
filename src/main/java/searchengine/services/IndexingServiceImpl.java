package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.exceptions.SIteUrlIsEmptyOrNullException;
import searchengine.model.Status;
import searchengine.ropositories.PageRepository;
import searchengine.ropositories.SiteRepository;
import searchengine.indexing.SiteMapTask;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    @Getter
    @Setter
    private AtomicBoolean indexingInProgress = new AtomicBoolean(false);
    private static final Logger logger = Logger.getLogger(IndexingServiceImpl.class.getName());
    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public IndexingResponse startIndexing() {
        if (indexingInProgress.get()) {
            return new IndexingResponse(false, "Indexing is already in progress");
        }
        List<Site> sites = sitesList.getSites();
        Set<String> visited = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newFixedThreadPool(sites.size());

        for (Site site : sites) {
            ForkJoinPool pool = new ForkJoinPool();
            if (site.getUrl() == null || site.getUrl().isEmpty()) {
                throw new SIteUrlIsEmptyOrNullException(site.getName());
            }
            deleteSiteWithPages(site);
            createSite(site);
            try {
                crawlSite(site, visited, pool);
            } catch (Exception e) {
                updateSiteStatus(site, Status.FAILED);
                logger.info("Error indexing site: " + site.getUrl() + " " + e.getMessage());
                return new IndexingResponse(false, e.getMessage());
            } finally {
                updateSiteStatus(site, Status.INDEXED);
            }

        }
        return new IndexingResponse(true, "Indexing completed successfully");
    }

    private void crawlSite(Site site, Set<String> visited, ForkJoinPool pool) throws URISyntaxException {
        String queryStr = pool.invoke(new SiteMapTask(site.getUrl(), getDomain(site.getUrl()), visited,1));
        jdbcTemplate.execute(queryStr);
    }

    private void deleteSiteWithPages(Site site) {
        searchengine.model.Site siteModel = siteRepository.findByUrl(site.getUrl());
        pageRepository.deleteAllBySiteId(siteModel.getId());
        siteRepository.delete(siteModel);
    }

    private void createSite(Site site) {
        searchengine.model.Site siteModel = new searchengine.model.Site();
        siteModel.setUrl(site.getUrl());
        siteModel.setName(site.getName());
        siteModel.setStatus(Status.INDEXING);
        siteModel.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteModel);
    }

    private static String getDomain(String url) throws URISyntaxException {
        if (url == null || url.isEmpty()) {
            return null;
        }
        URI uri = new URI(url);
        return uri.getHost();
    }

    private void updateSiteStatus(Site site, Status status) {
        searchengine.model.Site siteModel = siteRepository.findByUrl(site.getUrl());
        siteModel.setStatus(status);
        siteRepository.save(siteModel);
    }

    private void updateSiteStatusTime(Site site) {
        searchengine.model.Site siteModel = siteRepository.findByUrl(site.getUrl());
        siteModel.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteModel);
    }


}
