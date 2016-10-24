package de.exciteproject.refseg.train;

import java.util.List;

import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;

public class DirectPositionCounter extends Counter {

    public DirectPositionCounter(String[] tagLabelHierarchy, Positions positions, String constraintCountLabel) {
        super(tagLabelHierarchy, positions, constraintCountLabel);
    }

    @Override
    public void count(Goddag goddag, ConstraintCounts constraintCounts) {
        System.out.println(goddag);
        this.count(goddag.getRootNode(), 0, constraintCounts);
    }

    private void count(Node currentNode, int positionInLabelHierarchy, ConstraintCounts constraintCounts) {

        for (Node currentNodeChild : currentNode.getChildren()) {
            if (currentNodeChild.getLabel().equals(this.tagLabelHieararchy[positionInLabelHierarchy])) {
                System.out.println("win");
                if (positionInLabelHierarchy == (this.tagLabelHieararchy.length - 1)) {
                    List<Node> authorNodeChildren = currentNodeChild.getChildren();
                    for (int childeNodeIndex = 0; childeNodeIndex < authorNodeChildren.size(); childeNodeIndex++) {
                        System.out.println(childeNodeIndex + " " + authorNodeChildren.size());
                        if (this.positions.isValid(childeNodeIndex, (authorNodeChildren.size() - 1))) {
                            Node authorChild = authorNodeChildren.get(childeNodeIndex);
                            String nameWord = authorChild.getLabel();

                            constraintCounts.addCount(nameWord, this.constraintCountLabel, 1);
                        }
                    }
                } else {
                    this.count(currentNodeChild, positionInLabelHierarchy + 1, constraintCounts);
                }
            }
        }
    }
}