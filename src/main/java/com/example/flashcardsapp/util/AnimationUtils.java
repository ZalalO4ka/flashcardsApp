package com.example.flashcardsapp.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class AnimationUtils {

    public static void applyHoverEffect(Node node) {
        node.setOnMouseEntered(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(150), node);
            ft.setToValue(0.75);
            ft.play();
        });
        node.setOnMouseExited(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(150), node);
            ft.setToValue(1.0);
            ft.play();
        });
    }

    public static void animateFlip(Node cardNode, Runnable onHalfWay, Runnable onFinished) {
        cardNode.setRotationAxis(Rotate.Y_AXIS);

        RotateTransition flipFirst = new RotateTransition(Duration.millis(140), cardNode);
        flipFirst.setAxis(Rotate.Y_AXIS);
        flipFirst.setFromAngle(0);
        flipFirst.setToAngle(90);
        flipFirst.setInterpolator(Interpolator.EASE_IN);

        RotateTransition flipSecond = new RotateTransition(Duration.millis(140), cardNode);
        flipSecond.setAxis(Rotate.Y_AXIS);
        flipSecond.setFromAngle(-90);
        flipSecond.setToAngle(0);
        flipSecond.setInterpolator(Interpolator.EASE_OUT);

        flipFirst.setOnFinished(e -> {
            onHalfWay.run();
            flipSecond.play();
        });

        flipSecond.setOnFinished(e -> onFinished.run());
        flipFirst.play();
    }

    public static void animateSlide(Node cardNode, boolean isNext, Runnable onHalfWay, Runnable onFinished) {
        double offset = isNext ? -300 : 300;

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(150), cardNode);
        slideOut.setByX(offset);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), cardNode);
        fadeOut.setToValue(0.0);

        ParallelTransition exitAnim = new ParallelTransition(slideOut, fadeOut);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(150), cardNode);
        slideIn.setFromX(-offset);
        slideIn.setToX(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), cardNode);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ParallelTransition enterAnim = new ParallelTransition(slideIn, fadeIn);

        exitAnim.setOnFinished(e -> {
            onHalfWay.run();
            enterAnim.play();
        });

        enterAnim.setOnFinished(e -> {
            cardNode.setTranslateX(0);
            onFinished.run();
        });

        exitAnim.play();
    }
}