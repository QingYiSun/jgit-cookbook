package org.dstadler.jgit.sunspace;

import org.apache.commons.io.FileUtils;
import org.dstadler.jgit.helper.CookbookHelper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReadSpecificFile {

    private static final String REMOTE_URL = "https://github.com/spring-projects/spring-data-gemfire.git";
    private static final String pathName_head = "/Users/abnerallen/IdeaProjects/SunGit/jgit-cookbook/src/test";
    private static final String pathName_rear = "/gemfire";

    public static void main(String[] args) throws IOException, GitAPIException{

        // create temporary directory to store the git project
        File d = new File(pathName_head + pathName_rear);
        d.mkdirs();

        File localPath = new File(pathName_head + pathName_rear);
        localPath.createNewFile();

        // clone remote repository to local
        try(Repository repository = CookbookHelper.openJGitCookbookRepository()
        ){

            ObjectId lastCommitId = repository.resolve(Constants.HEAD);

            // a RevWalk allows to walk over commits based on some filtering that is defined
            try(RevWalk revWalk = new RevWalk(repository)){

                RevCommit commit = revWalk.parseCommit(lastCommitId);
                RevTree tree = commit.getTree(); // use commits' tree to find the path
                System.out.println("Having tree: " + tree);

                try(TreeWalk treeWalk = new TreeWalk(repository)){

                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathFilter.create("README.md"));
                    if(!treeWalk.next()){
                        throw new IllegalStateException("Did not find expected file 'README.md'");
                    }

                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);

                    loader.copyTo(System.out);

                }

            }

        }

        FileUtils.deleteDirectory(localPath);

    }

}
