package com.rosetta.cli;

import com.rosetta.services.AnalyzeRepositoryService;
import com.rosetta.storage.HibernateUtil;
import jakarta.persistence.EntityManager;
import com.rosetta.domain.MetricValue;

import picocli.CommandLine;  // ✅ correct package

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "rosetta",
        mixinStandardHelpOptions = true,
        version = "Rosetta 0.2.0",
        subcommands = { AnalyzeRepo.class, ListMetrics.class }
)
public class Main implements Callable<Integer> {
    public static void main(String[] args) {
        int exit = new CommandLine(new Main()).execute(args);
        System.exit(exit);
    }
    @Override public Integer call() {
        new CommandLine(this).usage(System.out);
        return 0;
    }
}

@CommandLine.Command(name = "analyze-repo", description = "Clone (or update) a repository and analyze metrics.")
class AnalyzeRepo implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "Git URL of the repository")
    String url;

    @CommandLine.Option(names = "--project", description = "Project name (default: derived from repo)", required = false)
    String projectName;

    @CommandLine.Option(names = "--user", description = "Username for private repo", required = false)
    String user;

    @CommandLine.Option(names = "--token", description = "Token/password for private repo", required = false)
    String token;

    @Override public Integer call() throws Exception {
        System.out.println("Analyzing repo: " + url);
        String name = (projectName != null && !projectName.isBlank())
                ? projectName
                : url.substring(url.lastIndexOf('/') + 1).replace(".git","");

        AnalyzeRepositoryService svc = new AnalyzeRepositoryService();
        svc.analyze(name, url, user, token);
        System.out.println("Done.");
        return 0;
    }
}

@CommandLine.Command(name = "list-metrics", description = "List stored metrics")
class ListMetrics implements Callable<Integer> {
    @Override public Integer call() {
        EntityManager em = HibernateUtil.emf().createEntityManager();
        try {
            em.createQuery("select mv from MetricValue mv", MetricValue.class)
              .getResultStream().forEach(mv -> {
                  String filePath = mv.getFile().getPath();
                  String mKey = mv.getMetric().getKey();
                  double v = mv.getValue();
                  System.out.printf("%s : %s = %s%n", filePath, mKey, v);
              });
        } finally {
            em.close();
        }
        return 0;
    }
}