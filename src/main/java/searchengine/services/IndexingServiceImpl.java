package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.ropositories.PageRepository;
import searchengine.ropositories.SiteRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    @Override
    public IndexingResponse startIndexing() {
        List<Site> sites = sitesList.getSites();
        for (Site site : sites) {
            deleteSiteWithPages(site);
            searchengine.model.Site siteModel = createSite(site);

        }
        return null;
    }

    private void deleteSiteWithPages(Site site) {
        String url = site.getUrl();
        searchengine.model.Site siteModel = siteRepository.findByUrl(url);
        if (siteModel != null) {
            pageRepository.deleteAllBySiteId(siteModel.getId());
            siteRepository.delete(siteModel);
        }
    }

    private searchengine.model.Site createSite(Site site) {
        if (site != null) {
            searchengine.model.Site siteModel = new searchengine.model.Site();
            siteModel.setUrl(site.getUrl());
            siteModel.setName(site.getName());
            return siteRepository.save(siteModel);
        }
        return null;
    }

}
