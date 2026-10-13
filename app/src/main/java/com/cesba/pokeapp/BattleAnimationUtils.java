package com.cesba.pokeapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.CycleInterpolator;
import android.widget.ImageView;

public class BattleAnimationUtils {

    public interface AnimationCallback {
        void onAnimationEnd();
    }

    public static void playAttackAnimation(
            ImageView attackerImg,
            ImageView defenderImg,
            boolean isPlayerAttacking,
            String moveType,
            String moveCategory,
            AnimationCallback callback) {

        if (attackerImg == null || defenderImg == null) {
            if (callback != null) callback.onAnimationEnd();
            return;
        }

        // 1. Attacker Motion Animation
        AnimatorSet attackerSet = new AnimatorSet();

        if ("special".equalsIgnoreCase(moveCategory)) {
            // Special Move: Charge pulse & scale up
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(attackerImg, "scaleX", 1.0f, 1.25f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(attackerImg, "scaleY", 1.0f, 1.25f, 1.0f);
            attackerSet.playTogether(scaleX, scaleY);
            attackerSet.setDuration(450);
        } else if ("status".equalsIgnoreCase(moveCategory)) {
            // Status Move: Gentle double bounce
            float moveY = isPlayerAttacking ? -25f : 25f;
            ObjectAnimator bounce = ObjectAnimator.ofFloat(attackerImg, "translationY", 0f, moveY, 0f, moveY, 0f);
            bounce.setDuration(500);
            attackerSet.play(bounce);
        } else {
            // Physical Move: Fast Lunge / Charge toward opponent
            float lungeX = isPlayerAttacking ? 50f : -50f;
            float lungeY = isPlayerAttacking ? -50f : 50f;

            ObjectAnimator moveOutX = ObjectAnimator.ofFloat(attackerImg, "translationX", 0f, lungeX, 0f);
            ObjectAnimator moveOutY = ObjectAnimator.ofFloat(attackerImg, "translationY", 0f, lungeY, 0f);
            moveOutX.setInterpolator(new AccelerateDecelerateInterpolator());
            moveOutY.setInterpolator(new AccelerateDecelerateInterpolator());
            attackerSet.playTogether(moveOutX, moveOutY);
            attackerSet.setDuration(400);
        }

        attackerSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Reset attacker transforms
                attackerImg.setTranslationX(0f);
                attackerImg.setTranslationY(0f);
                attackerImg.setScaleX(1.0f);
                attackerImg.setScaleY(1.0f);

                // 2. Defender Impact Shake & Flash
                playDefenderHitAnimation(defenderImg, moveType, callback);
            }
        });

        attackerSet.start();
    }

    private static void playDefenderHitAnimation(ImageView defenderImg, String moveType, AnimationCallback callback) {
        // Rapid Shake
        ObjectAnimator shakeX = ObjectAnimator.ofFloat(defenderImg, "translationX", 0f, 25f, -25f, 20f, -20f, 10f, -10f, 0f);
        shakeX.setInterpolator(new CycleInterpolator(2));
        shakeX.setDuration(450);

        // Flashing Alpha / Blink Impact
        ObjectAnimator blink = ObjectAnimator.ofFloat(defenderImg, "alpha", 1.0f, 0.2f, 1.0f, 0.2f, 1.0f);
        blink.setDuration(450);

        // Type Tint Color Filter
        int typeColor = TypeColorUtils.getTypeColor(moveType);
        int semiTransColor = Color.argb(160, Color.red(typeColor), Color.green(typeColor), Color.blue(typeColor));
        defenderImg.setColorFilter(semiTransColor, PorterDuff.Mode.SRC_ATOP);

        AnimatorSet defenderSet = new AnimatorSet();
        defenderSet.playTogether(shakeX, blink);

        defenderSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                defenderImg.clearColorFilter();
                defenderImg.setAlpha(1.0f);
                defenderImg.setTranslationX(0f);

                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }
        });

        defenderSet.start();
    }
}