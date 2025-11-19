package searchengine.indexing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

public class SiteMapTask extends RecursiveTask<String> {
    private String url;
    private final String domain;
    private final Set<String> visitedUrl;
    private final int depth;

    public SiteMapTask(String url, String domain, Set<String> visitedUrl, int depth) {
        this.url = url;
        this.domain = domain;
        this.visitedUrl = visitedUrl;
        this.depth = depth;
    }

    @Override
    protected String compute() {
        List<SiteMapTask> map = new ArrayList<>();

        url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;

        if (!url.startsWith("http") || !url.contains(domain) || url.contains("#") || !visitedUrl.add(url)) {
            //System.out.println("Игнорируем - " + url);
            return "";
        }

        try {
            //System.out.println("Обход: " + url);

            Document document = Jsoup.connect(url).get();

            Elements links = document.select("a[href]");
            for (Element link : links) {
                String absUrl = link.absUrl("href");
                absUrl = absUrl.endsWith("/") ? absUrl.substring(0, absUrl.length() - 1) : absUrl;

                if (!absUrl.startsWith("http") || visitedUrl.contains(absUrl)) {
                    continue;
                }
                //System.out.println("Найдена ссылка: " + absUrl);

                SiteMapTask task = new SiteMapTask(absUrl, domain, visitedUrl, depth + 1);
                task.fork();
                map.add(task);
            }

        } catch (IOException e) {
            System.out.println("Ошибка: " + url);
        }

        StringBuilder result = new StringBuilder("\t".repeat(depth) + url + "\n");
        for (SiteMapTask task : map) {
            result.append(task.join());
        }
        return result.toString();
    }
}

