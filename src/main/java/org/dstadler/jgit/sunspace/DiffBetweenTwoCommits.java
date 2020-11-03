package org.dstadler.jgit.sunspace;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DiffBetweenTwoCommits {

    private static final String REMOTE_URL = "https://github.com/spring-projects/spring-data-redis.git";

    public static void main(String[] args) throws IOException, GitAPIException{

        File localPath = new File("/Users/abnerallen/jgit-cookbook/src/test/redis");
        localPath.createNewFile();

        System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);

        try(Git git = Git.cloneRepository()
            .setURI(REMOTE_URL)
            .setDirectory(localPath)
            .call()
        ){
            System.out.println("Having repository: " + git.getRepository().getDirectory());
            listDiff(
                    git.getRepository(),
                    git,
                    "24189621b0ec133b5937d16b6a0be0a2234c1179",
                    "23e31f3a88f6611ab553ac5d6ec62bea1389d841"
            );
        }

    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException{

        try(RevWalk walk = new RevWalk(repository)){
            RevCommit commit = walk.parseCommit(repository.resolve(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try(ObjectReader reader = repository.newObjectReader()){
                treeParser.reset(reader, tree.getId());
            }
            walk.dispose();
            return treeParser;
        }

    }

    private static void listDiff(Repository repository, Git git, String oldCommit, String newCommit) throws GitAPIException, IOException{

        final List<DiffEntry> diffs = git.diff()
                .setOldTree(prepareTreeParser(repository, oldCommit))
                .setNewTree(prepareTreeParser(repository, newCommit))
                .call();

        System.out.println("Found: " + diffs.size() + " differences");

        for(DiffEntry diff: diffs){
            System.out.println("Diff: " + diff.getChangeType() + ": " +
                    (diff.getOldPath().equals(diff.getNewPath()) ? diff.getNewPath() : diff.getOldPath() + " -> " + diff.getNewPath()));
        }

    }

}
