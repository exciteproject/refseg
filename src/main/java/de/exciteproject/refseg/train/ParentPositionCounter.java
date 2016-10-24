package de.exciteproject.refseg.train;

import java.util.List;

import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;

public class ParentPositionCounter extends Counter {

    public ParentPositionCounter(String[] tagLabelHierarchy, Positions positions, String constraintCountLabel) {
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
                if (positionInLabelHierarchy == (this.tagLabelHieararchy.length - 2)) {
                    List<Node> lastLevelNodes = currentNodeChild.getChildren();
                    for (int lastLevelNodeIndex = 0; lastLevelNodeIndex < lastLevelNodes.size(); lastLevelNodeIndex++) {
                        if (lastLevelNodes.get(lastLevelNodeIndex).getLabel()
                                .equals(this.tagLabelHieararchy[this.tagLabelHieararchy.length - 1])) {
                            if (this.positions.isValid(lastLevelNodeIndex, (lastLevelNodes.size() - 1))) {
                                Node authorChild = lastLevelNodes.get(lastLevelNodeIndex).getFirstChild();
                                String nameWord = authorChild.getLabel();

                                constraintCounts.addCount(nameWord, this.constraintCountLabel, 1);
                            }
                        }
                    }
                } else {
                    this.count(currentNodeChild, positionInLabelHierarchy + 1, constraintCounts);
                }
            }
        }
    }
}
