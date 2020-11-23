package org.dstadler.jgit.sunspace;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GetLineNum {

    private static final String REMOTE_URL = "https://github.com/spring-projects/spring-data-geode.git";
    private static final String path_head = "/Users/abnerallen/IdeaProjects/SunGit/jgit-cookbook/src/test";
    private static final String path_rear = "/geode";

    public static void main(String[] args) throws IOException, GitAPIException{

        File newDir = new File(path_head + path_rear);
        newDir.mkdirs();
        File localPath = new File(path_head + path_rear);
        localPath.createNewFile();

        try(Git git = Git.cloneRepository()
            .setURI(REMOTE_URL)
            .setDirectory(localPath)
            .call()
        ){

            Collection<Ref> allRefs = git.getRepository().getAllRefs().values();

            int counter = 0;
            ArrayList<RevCommit> allCommits = new ArrayList<>();

            try(RevWalk revWalk = new RevWalk(git.getRepository())){

                for(Ref ref: allRefs){
                    revWalk.markStart(revWalk.parseCommit(ref.getObjectId()));
                }

                for(RevCommit commit: revWalk){
                    allCommits.add(commit);
                    counter++;
                }

            }

            for(int i = allCommits.size() - 1; i >= 1; i--){

                ObjectReader reader = git.getRepository().newObjectReader();
                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                ObjectId oldTree = git.getRepository().resolve(allCommits.get(i).getId().getName() + "^{tree}");
                oldTreeIter.reset(reader, oldTree);
                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                ObjectId newTree = git.getRepository().resolve(allCommits.get(i - 1).getId().getName() + "^{tree}");
                newTreeIter.reset(reader, newTree);

                System.out.println("Commit Message: " + allCommits.get(i - 1).getFullMessage());

                final List<DiffEntry> diffs = git.diff()
                        .setOldTree(prepareTreeParser(git.getRepository(), allCommits.get(i).getId().getName()))
                        .setNewTree(prepareTreeParser(git.getRepository(), allCommits.get(i - 1).getId().getName()))
                        .call();
                for(DiffEntry diff: diffs){
                    System.out.println("Diff: " + diff.getChangeType() + ": " + (diff.getOldPath().equals(diff.getNewPath()) ? diff.getNewPath() : diff.getOldPath() + " -> " + diff.getNewPath()));
                }

            }

        }

        FileUtils.deleteDirectory(localPath);

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

}
