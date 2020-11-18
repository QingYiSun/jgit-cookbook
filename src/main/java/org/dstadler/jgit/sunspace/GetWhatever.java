package org.dstadler.jgit.sunspace;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GetWhatever {

    private static final String REMOTE_URL = "https://github.com/spring-projects/spring-data-gemfire.git";

    private static final String path_head = "/Users/abnerallen/IdeaProjects/SunGit/jgit-cookbook/src/test";
    private static final String path_rear = "/gemfire";

    private static final String SPLIT_SYM = "#########################";

    public static void main(String[] args) throws IOException, GitAPIException{

        File newDir = new File(path_head + path_rear);
        newDir.mkdirs();
        File localPath = new File(path_head + path_rear);
        localPath.createNewFile();

        OutputStream os = new FileOutputStream("/Users/abnerallen/Desktop/gemfire.txt");
        PrintWriter pw = new PrintWriter(os);

        try(Git git = Git.cloneRepository()
            .setURI(REMOTE_URL)
            .setDirectory(localPath)
            .call()
        ){

            Collection<Ref> allRefs = git.getRepository().getAllRefs().values();

            int counter = 0; // count the number of commits in one repository
            ArrayList<ObjectId> allCommitIDs = new ArrayList<>(); // store the ID of each commit
            ArrayList<String> CommitMSGs = new ArrayList<>(); // store all the commits' messages
            ArrayList<RevCommit> allCommits = new ArrayList<>(); // store all the commits

            // get all the commits
            try(RevWalk revWalk = new RevWalk(git.getRepository())){

                for(Ref ref: allRefs){
                    revWalk.markStart(revWalk.parseCommit(ref.getObjectId()));
                }

                for(RevCommit commit: revWalk){
                    allCommits.add(commit);
                    CommitMSGs.add(commit.getFullMessage());
                    allCommitIDs.add(commit.getId());
                    counter++;
                }

            }

            // get all the diff entries
            for(int i = allCommitIDs.size() - 1; i >= 1; i--){

                ObjectReader reader = git.getRepository().newObjectReader();

                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                ObjectId oldTree = git.getRepository().resolve(allCommitIDs.get(i).getName() + "^{tree}");
                oldTreeIter.reset(reader, oldTree);

                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                ObjectId newTree = git.getRepository().resolve(allCommitIDs.get(i - 1).getName() + "^{tree}");
                newTreeIter.reset(reader, newTree);

                // write id and msg into files
                String currentId = allCommitIDs.get(i - 1).getName();
                String currentMSG = allCommits.get(i - 1).getFullMessage();
                pw.println(currentId);
                pw.println(currentMSG);

                // show changed files between commits: oldCommit(i), newCommit(i - 1)[current]
                final List<DiffEntry> diffs = git.diff()
                        .setOldTree(prepareTreeParser(git.getRepository(), allCommitIDs.get(i).getName()))
                        .setNewTree(prepareTreeParser(git.getRepository(), allCommitIDs.get(i - 1).getName()))
                        .call();
                // write all the diff info into files
                for(DiffEntry diff: diffs){
                    pw.println("Diff: " + diff.getChangeType() + ": " +
                                    (diff.getOldPath().equals(diff.getNewPath()) ? diff.getNewPath() : diff.getOldPath() + " -> " + diff.getNewPath())
                            );
                }
                DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
                diffFormatter.setRepository(git.getRepository());
                List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

                for(DiffEntry entry: entries){
                    if(entry.getChangeType().equals(DiffEntry.ChangeType.MODIFY)) {
                        pw.println("^MODIFY$: ");
                        String tmpNew = readFile(git.getRepository(), allCommits.get(i - 1), entry.getNewPath());
                        String tmpOld = readFile(git.getRepository(), allCommits.get(i), entry.getOldPath());
                        pw.println(tmpOld);
                        pw.println(tmpNew);
                    }
                    else if(entry.getChangeType().equals(DiffEntry.ChangeType.ADD)){
                        String tmpNew = readFile(git.getRepository(), allCommits.get(i - 1), entry.getNewPath());
                        pw.println("^ADD$: ");
                        pw.println(tmpNew);
                    }
                    else if(entry.getChangeType().equals(DiffEntry.ChangeType.DELETE)){
                        String tmpOld = readFile(git.getRepository(), allCommits.get(i), entry.getOldPath());
                        pw.println("^DELETE$: ");
                        pw.println(tmpOld);
                    }
                }

                pw.println(SPLIT_SYM);

            }

        }

        pw.close();
        os.close();

        FileUtils.deleteDirectory(localPath);

    }

    private static String readFile(Repository repo, RevCommit commit, String filePath) throws IOException{
        try(TreeWalk walk = TreeWalk.forPath(repo, filePath, commit.getTree())){
            if(null != walk){
                byte[] bytes = repo.open(walk.getObjectId(0)).getBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            }
            else{
                throw new IllegalArgumentException("No path found.");
            }
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

}
