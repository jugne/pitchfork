package pitchfork.operators;

import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.TreeParser;
import junit.framework.Assert;
import org.junit.Test;

public class SubtreeSlideOperatorTest {

    @Test
    public void testComputeYoungerAttachmentPointProb1() {

        Tree tree = new TreeParser("((A:1,B:1):1,C:3):0.0");

        SubtreeSlideOperator stsOp = new SubtreeSlideOperator();
        stsOp.initByName(
                "tree",tree,
                "relSize",0.15,
                "probCoalAttach", 0.0,
                "weight", 1.0);

        SubtreeSlideOperator.AttachmentPoint ap = stsOp.new AttachmentPoint();
        Node edgeParentNode = tree.getRoot();
        ap.attachmentEdgeBase = tree.getNode(0);
        ap.attachmentHeight = 1.2;

        stsOp.computeYoungerAttachmentPointProb(ap, edgeParentNode, stsOp.getCurrentLambda());

        Assert.assertEquals(-3.894639484342174, ap.logProb, 1e-10);
    }

    @Test
    public void testComputeOlderAttchmentPointProb1() {
        Tree tree = new TreeParser("((A:1,B:1):1,C:3):0.0");

        SubtreeSlideOperator stsOp = new SubtreeSlideOperator();
        stsOp.initByName(
                "tree",tree,
                "relSize",0.15,
                "probCoalAttach", 0.0,
                "weight", 1.0);

        SubtreeSlideOperator.AttachmentPoint ap = stsOp.new AttachmentPoint();
        Node edgeParentNode = tree.getNode(0);
        ap.attachmentEdgeBase = tree.getRoot();
        ap.attachmentHeight = 3.7;

        stsOp.computeOlderAttachmentPointProb(ap, edgeParentNode, stsOp.getCurrentLambda());

        System.out.println(ap.logProb);
    }
}