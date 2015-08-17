/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.ui.codevisualization;

import eu.mihosoft.vrl.lang.model.Argument;
import eu.mihosoft.vrl.lang.model.ArgumentType;
import eu.mihosoft.vrl.lang.model.CodeEntity;
import eu.mihosoft.vrl.lang.model.CodeEvent;
import eu.mihosoft.vrl.lang.model.CodeEventType;
import eu.mihosoft.vrl.lang.model.Argument;
import eu.mihosoft.vrl.lang.model.Invocation;
import eu.mihosoft.vrl.lang.model.MethodDeclaration;
import eu.mihosoft.vrl.lang.model.Scope;
import eu.mihosoft.vrl.lang.model.ScopeInvocation;
import eu.mihosoft.vrl.lang.model.Type;
import eu.mihosoft.vrl.v3d.jcsg.CSG;
import eu.mihosoft.vrl.v3d.jcsg.MeshContainer;
import eu.mihosoft.vrl.v3d.jcsg.VFX3DUtil;
import eu.mihosoft.vrl.workflow.VFlow;
import eu.mihosoft.vrl.workflow.VNode;
import eu.mihosoft.vrl.workflow.fx.FXSkinFactory;
import eu.mihosoft.vrl.workflow.fx.ScaleBehavior;
import eu.mihosoft.vrl.workflow.fx.TranslateBehavior;
import eu.mihosoft.vrl.workflow.fx.VCanvas;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class VariableFlowNodeSkin extends CustomFlowNodeSkin {

    private boolean updating;
    private VBox outputs;

    public VariableFlowNodeSkin(FXSkinFactory skinFactory, VNode model, VFlow controller) {
        super(skinFactory, model, controller);
    }

    @Override
    public void configureCanvas(VCanvas canvas) {
        super.configureCanvas(canvas);

        canvas.setScaleBehavior(ScaleBehavior.ALWAYS);
        canvas.setTranslateBehavior(TranslateBehavior.ALWAYS);
    }

    @Override
    protected Node createView() {

        ComboBox<MethodDeclaration> box = new ComboBox<>();

        VBox parent = new VBox(box);

        parent.setAlignment(Pos.CENTER);

        Object value = getModel().getValueObject().getValue();

        if (value instanceof ScopeInvocation) {

        }

        if (value instanceof Invocation) {

//            if (value instanceof ScopeInvocation) {
//                ScopeInvocation inv = (ScopeInvocation) value;
//                Scope s = inv.getScope();
//                
//                if (s instanceof ForDeclaration) {
//                    System.out.println("SCOPE: " + s);
//                    System.exit(1);
//                }
//            }
            Invocation invocation = (Invocation) value;

            box.setVisible(!invocation.getArguments().isEmpty());

            VBox inputs = new VBox();
            outputs = new VBox();
            HBox hbox = new HBox(inputs, outputs);
            hbox.setPadding(new Insets(0, 15, 0, 15));

            createArgView(invocation, inputs, false);

            invocation.getArguments().addListener((ListChangeListener.Change<? extends Argument> c) -> {
                        box.setVisible(!invocation.getArguments().isEmpty());
                        if (!updating) {
                            createArgView(invocation, inputs,
                                    invocation.getArguments().size()
                                    == inputs.getChildren().size());
                        }
                    });
            parent.getChildren().add(hbox);
        }
        
        

        if (getModel().getValueObject().getValue() instanceof CodeEntity) {
            CodeEntity ce = (CodeEntity) getModel().getValueObject().getValue();
            updateOutputView(ce.getMetaData().get("VRL:retVal"));
            
            ce.getMetaData().addListener((Observable observable) -> {
                updateOutputView(ce.getMetaData().get("VRL:retVal"));
            });
        }

        getModel().getValueObject().valueProperty().addListener((ov, oldV, newV) -> {
            if (newV instanceof CodeEntity) {
                CodeEntity ce = (CodeEntity) newV;
                ce.getMetaData().addListener((Observable observable) -> {
                    updateOutputView(ce.getMetaData().get("VRL:retVal"));
                });
            }
        });

        return parent;
    }

    private void setFieldListener(int argIndex, TextField field, Invocation invocation, Argument a) {
        field.textProperty().addListener((ov, oldV, newV) -> {
            try {
                Integer intValue = Integer.parseInt(newV);

                invocation.getArguments().set(argIndex,
                        Argument.constArg(Type.INT, intValue));
                invocation.getParent().fireEvent(new CodeEvent(
                        CodeEventType.CHANGE, invocation.getParent()));

                if (invocation instanceof ScopeInvocation) {
                    ScopeInvocation scopeInv = (ScopeInvocation) invocation;
                    Scope scope = scopeInv.getScope();

                }
            } catch (Exception ex) {

            }
        });
//        EventStream<Change<String>> textEvents
//                = EventStreams.changesOf(field.textProperty());
//
//        textEvents.reduceSuccessions((red1, red2) -> red2, Duration.ofMillis(1000)).
//                subscribe(text -> {
//                    try {
//                        Integer intValue = Integer.parseInt(text.getNewValue());
//
//                        invocation.getArguments().set(argIndex,
//                                Argument_Impl.constArg(Type.INT, intValue));
//                        invocation.getParent().fireEvent(new CodeEvent(
//                                        CodeEventType.CHANGE, invocation.getParent()));
//
//                    } catch (Exception ex) {
//                        //
//                    }
//                });
    }

    private void createArgView(Invocation invocation, VBox inputs, boolean update) {

        updating = true;

        if (!update) {
            inputs.getChildren().clear();
        }

        int argIndex = 0;
        for (Argument a : invocation.getArguments()) {
            if (a.getArgType() == ArgumentType.CONSTANT || a.getArgType() == ArgumentType.NULL) {
                TextField field;

                if (update && inputs.getChildren().get(argIndex) instanceof TextField) {
                    field = (TextField) inputs.getChildren().get(argIndex);

                } else {
                    field = new TextField();

                    setFieldListener(argIndex, field, invocation, a);

                    if (update) {
                        inputs.getChildren().set(argIndex, field);

                    } else {
                        inputs.getChildren().add(field);
                    }
                }

                a.getConstant().ifPresent(o -> {

                    if (!field.getText().equals(o.toString())) {
                        field.setText(o.toString());
                    }
                });

            } else if (a.getArgType() == ArgumentType.VARIABLE) {
                Label label = new Label();
                a.getVariable().ifPresent(v -> label.setText(v.getName()));
                label.setTextFill(Color.WHITE);
                if (update) {
                    inputs.getChildren().set(argIndex, label);
                } else {
                    inputs.getChildren().add(label);
                }
            } else if (a.getArgType() == ArgumentType.INVOCATION) {
                Label label = new Label();
                label.setTextFill(Color.WHITE);
                a.getInvocation().ifPresent(i -> label.setText(i.getMethodName()));
                if (update) {
                    inputs.getChildren().set(argIndex, label);
                } else {
                    inputs.getChildren().add(label);
                }
            }

            argIndex++;
        } // end for

        updating = false;
    }

    private void setMeshScale(MeshContainer meshContainer, Bounds t1, final MeshView meshView) {
        double maxDim
                = Math.max(meshContainer.getWidth(),
                        Math.max(meshContainer.getHeight(), meshContainer.getDepth()));

        double minContDim = Math.min(t1.getWidth(), t1.getHeight());

        double scale = minContDim / (maxDim * 2);

        //System.out.println("scale: " + scale + ", maxDim: " + maxDim + ", " + meshContainer);
        meshView.setScaleX(scale);
        meshView.setScaleY(scale);
        meshView.setScaleZ(scale);
    }

    void updateOutputView(Object retVal) {
        if (outputs == null) {
            return;
        }

        outputs.getChildren().clear();

        CSG csg;

        if (retVal instanceof CSG) {
            csg = (CSG) retVal;

            Group viewGroup = new Group();

            SubScene subScene = new SubScene(viewGroup, 300, 200, true, SceneAntialiasing.DISABLED);
//            subScene.setFill(Color.WHEAT);
            PerspectiveCamera subSceneCamera = new PerspectiveCamera(false);
            subScene.setCamera(subSceneCamera);

            MeshContainer mc = csg.toJavaFXMesh();

            MeshView mv = mc.getAsMeshViews().get(0);
//            mv.setScaleX(10);
//            mv.setScaleY(10);
//            mv.setScaleZ(10);

            setMeshScale(mc, subScene.getBoundsInLocal(), mv);

            VFX3DUtil.addMouseBehavior(mv, subScene, MouseButton.PRIMARY);

            viewGroup.layoutXProperty().bind(subScene.widthProperty().divide(2));
            viewGroup.layoutYProperty().bind(subScene.heightProperty().divide(2));

            viewGroup.getChildren().addAll(mv);

            outputs.getChildren().addAll(subScene);
        } else if (retVal != null) {
            outputs.getChildren().add(new Label(retVal.toString()));
        } else {
            outputs.getChildren().add(new Label("null"));
        }

    }
}
