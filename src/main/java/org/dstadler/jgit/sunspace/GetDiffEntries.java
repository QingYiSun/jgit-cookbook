package org.dstadler.jgit.sunspace;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GetDiffEntries {

    private static final String REMOTE_URL = "https://github.com/spring-projects/spring-data-redis.git";

    public static void main(String[] args) throws IOException, GitAPIException{

        File localPath = new File("/Users/abnerallen/IdeaProjects/SunGit/jgit-cookbook/src/test/redis");
        localPath.createNewFile();

        System.out.println("正在从：" + REMOTE_URL + " 克隆到：" + localPath);

        try(Git git = Git.cloneRepository()
            .setURI(REMOTE_URL)
            .setDirectory(localPath)
            .call()
        ){

            System.out.println("已有本地仓库：" + git.getRepository().getDirectory());

            ObjectReader reader = git.getRepository().newObjectReader();

            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            ObjectId oldTree = git.getRepository().resolve("aa7f6d1032ce0894c144db33fa00b380712f9813^{tree}");
            oldTreeIter.reset(reader, oldTree);

            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            ObjectId newTree = git.getRepository().resolve("24189621b0ec133b5937d16b6a0be0a2234c1179^{tree}");
            newTreeIter.reset(reader, newTree);

            DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setRepository(git.getRepository());
            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

            int count = 0;
            for(DiffEntry entry: entries){
                count++;
                System.out.println(entry.getChangeType() + "; " + entry.getOldPath() + "; " + entry.getNewPath());
            }
            System.out.println("num of items: " + count);

        }

    }

}
