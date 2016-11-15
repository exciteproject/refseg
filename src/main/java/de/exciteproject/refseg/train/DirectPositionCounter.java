package de.exciteproject.refseg.train;

import java.util.List;

import de.mkrnr.goddag.Goddag;
import de.mkrnr.goddag.Node;

/**
 * Counter for iterating a Goddag graph and counting the label of the parent of
 * a leaf node. The position is based on the position of the leafnode relative
 * to the parent. This is needed, for example, for title and publisher labels.
 * <p>
 * TODO refactoring or better documentation of count classes
 */
public class DirectPositionCounter extends Counter {

    public DirectPositionCounter(String[] tagLabelHierarchy, Positions positions, String constraintCountLabel) {
        super(tagLabelHierarchy, positions, constraintCountLabel);
    }

    @Override
    public void count(Goddag goddag, ConstraintCounts constraintCounts) {
        this.count(goddag.getRootNode(), 0, constraintCounts);
    }

    private void count(Node currentNode, int positionInLabelHierarchy, ConstraintCounts constraintCounts) {

        for (Node currentNodeChild : currentNode.getChildren()) {
            if (currentNodeChild.getLabel().equals(this.tagLabelHieararchy[positionInLabelHierarchy])) {
                if (positionInLabelHierarchy == (this.tagLabelHieararchy.length - 1)) {
                    List<Node> authorNodeChildren = currentNodeChild.getChildren();
                    for (int childeNodeIndex = 0; childeNodeIndex < authorNodeChildren.size(); childeNodeIndex++) {
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
