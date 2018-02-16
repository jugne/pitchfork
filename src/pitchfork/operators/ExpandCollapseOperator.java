package pitchfork.operators;

import beast.core.Input;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.util.Randomizer;
import pitchfork.util.Pitchforks;

import java.util.ArrayList;
import java.util.List;

public class ExpandCollapseOperator extends PitchforkTreeOperator {

    Input<Double> lambdaInput = new Input<>("lambda",
            "Mean of exponential (relative to tree height) from which " +
                    "expanded node height is drawn if expanded from a " +
                    "root polytomy.",
            0.1);

    Tree tree;
    double lambda;

    @Override
    public void initAndValidate() {
        tree = treeInput.get();
        lambda = lambdaInput.get();
    }

    @Override
    public double proposal() {

        double logHR = 0.0;

        if (Randomizer.nextBoolean()) {
            // Collapse

            List<Node> collapsableEdges = getCollapsableEdges(tree);

            if (collapsableEdges.isEmpty())
                return Double.NEGATIVE_INFINITY;

            Node edgeToCollapse = collapsableEdges.get(Randomizer.nextInt(collapsableEdges.size()));
            //logHR -= Math.log(1.0/collapsableEdges.size());

            Node sister = getOtherChild(edgeToCollapse.getParent(), edgeToCollapse);

            edgeToCollapse.getParent().setHeight(sister.getHeight());

        } else {
            // Expand

            List<Node> expandableEdges = getExpandableEdges(tree);

            if (expandableEdges.isEmpty())
                return Double.NEGATIVE_INFINITY;

            Node edgeToExpand = expandableEdges.get(Randomizer.nextInt(expandableEdges.size()));

            Node logicalParent = Pitchforks.getLogicalParent(edgeToExpand);


            double newHeight;
            if (logicalParent.isRoot()) {

                double expRate = 1.0/(lambda*edgeToExpand.getHeight());
                newHeight = edgeToExpand.getHeight() + Randomizer.nextExponential(expRate);

                //logHR -= -expRate*(newHeight - edgeToExpand.getHeight()) + Math.log(expRate);

            } else {

                double L = logicalParent.getParent().getHeight() - logicalParent.getHeight();
                newHeight = logicalParent.getHeight() + Randomizer.nextDouble()*L;

                //logHR -= Math.log(1.0/L);
            }

            // Disconnect edge

            Node nodeToMove = edgeToExpand.getParent();
            Node sisterNode = getOtherChild(nodeToMove, edgeToExpand);
            Node nodeToMoveParent = nodeToMove.getParent();

            nodeToMove.removeChild(sisterNode);
            if (nodeToMoveParent != null) {
                nodeToMoveParent.removeChild(nodeToMove);
                nodeToMoveParent.addChild(sisterNode);
            } else {
                sisterNode.setParent(null);
            }
            nodeToMove.setParent(null);

            // Attach edge

            if (sisterNode.isRoot()) {
                nodeToMove.addChild(sisterNode);
                tree.setRoot(nodeToMove);
             } else {
                if (logicalParent.isRoot()) {
                    nodeToMove.addChild(logicalParent);
                    tree.setRoot(nodeToMove);
                } else {
                    Node logicalParentParent = logicalParent.getParent();
                    logicalParentParent.removeChild(logicalParent);
                    logicalParentParent.addChild(nodeToMove);
                    nodeToMove.addChild(logicalParent);
                }
            }

            // Set new node height
            nodeToMove.setHeight(newHeight);
        }


        return logHR;
    }

    private List<Node> getCollapsableEdges(Tree tree) {

        List<Node> trueNodes = Pitchforks.getTrueNodes(tree);
        List<Node> collapsableEdges = new ArrayList<>();

        for (Node node : trueNodes) {
            if (!node.isRoot()
                    && !Pitchforks.isPolytomy(node.getParent())
                    && getOtherChild(node.getParent(), node).getHeight() > node.getHeight())
                collapsableEdges.add(node);
        }

        return collapsableEdges;
    }

    private List<Node> getExpandableEdges(Tree tree) {

        List<Node> trueNodes = Pitchforks.getTrueNodes(tree);
        List<Node> expandableEdges = new ArrayList<>();

        for (Node node : trueNodes) {
            if (!node.isRoot() && Pitchforks.isPolytomy(node.getParent()))
                expandableEdges.add(node);
        }

        return expandableEdges;
    }
}