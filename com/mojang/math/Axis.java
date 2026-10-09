/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Vector3fc
 */
package com.mojang.math;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3fc;

public interface Axis {
    public static final Axis XN = new Axis(){

        @Override
        public Quaternionf rotation(float angle) {
            return new Quaternionf().rotationX(-angle);
        }

        @Override
        public Matrix3f rotate(Matrix3f matrix, float angle) {
            return matrix.rotateX(-angle);
        }

        @Override
        public Matrix4f rotate(Matrix4f matrix, float angle) {
            return matrix.rotateX(-angle);
        }

        public String toString() {
            return "<rotation around -X>";
        }
    };
    public static final Axis XP = new Axis(){

        @Override
        public Quaternionf rotation(float angle) {
            return new Quaternionf().rotationX(angle);
        }

        @Override
        public Matrix3f rotate(Matrix3f matrix, float angle) {
            return matrix.rotateX(angle);
        }

        @Override
        public Matrix4f rotate(Matrix4f matrix, float angle) {
            return matrix.rotateX(angle);
        }

        public String toString() {
            return "<rotation around +X>";
        }
    };
    public static final Axis YN = new Axis(){

        @Override
        public Quaternionf rotation(float angle) {
            return new Quaternionf().rotationY(-angle);
        }

        @Override
        public Matrix3f rotate(Matrix3f matrix, float angle) {
            return matrix.rotateY(-angle);
        }

        @Override
        public Matrix4f rotate(Matrix4f matrix, float angle) {
            return matrix.rotateY(-angle);
        }

        public String toString() {
            return "<rotation around -Y>";
        }
    };
    public static final Axis YP = new Axis(){

        @Override
        public Quaternionf rotation(float angle) {
            return new Quaternionf().rotationY(angle);
        }

        @Override
        public Matrix3f rotate(Matrix3f matrix, float angle) {
            return matrix.rotateY(angle);
        }

        @Override
        public Matrix4f rotate(Matrix4f matrix, float angle) {
            return matrix.rotateY(angle);
        }

        public String toString() {
            return "<rotation around +Y>";
        }
    };
    public static final Axis ZN = new Axis(){

        @Override
        public Quaternionf rotation(float angle) {
            return new Quaternionf().rotationZ(-angle);
        }

        @Override
        public Matrix3f rotate(Matrix3f matrix, float angle) {
            return matrix.rotateZ(-angle);
        }

        @Override
        public Matrix4f rotate(Matrix4f matrix, float angle) {
            return matrix.rotateZ(-angle);
        }

        public String toString() {
            return "<rotation around -Z>";
        }
    };
    public static final Axis ZP = new Axis(){

        @Override
        public Quaternionf rotation(float angle) {
            return new Quaternionf().rotationZ(angle);
        }

        @Override
        public Matrix3f rotate(Matrix3f matrix, float angle) {
            return matrix.rotateZ(angle);
        }

        @Override
        public Matrix4f rotate(Matrix4f matrix, float angle) {
            return matrix.rotateZ(angle);
        }

        public String toString() {
            return "<rotation around +Z>";
        }
    };

    public static Axis of(final Vector3fc axis) {
        return new Axis(){

            @Override
            public Quaternionf rotation(float angle) {
                return new Quaternionf().rotationAxis(angle, axis);
            }

            @Override
            public Matrix3f rotate(Matrix3f matrix, float angle) {
                return matrix.rotate(angle, axis);
            }

            @Override
            public Matrix4f rotate(Matrix4f matrix, float angle) {
                return matrix.rotate(angle, axis);
            }

            public String toString() {
                return "<rotation around " + String.valueOf(axis) + ">";
            }
        };
    }

    public Quaternionf rotation(float var1);

    default public Quaternionf rotationDegrees(float angle) {
        return this.rotation(angle * ((float)Math.PI / 180));
    }

    public Matrix3f rotate(Matrix3f var1, float var2);

    default public Matrix3f rotateDegrees(Matrix3f matrix, float angle) {
        return this.rotate(matrix, angle * ((float)Math.PI / 180));
    }

    public Matrix4f rotate(Matrix4f var1, float var2);

    default public Matrix4f rotateDegrees(Matrix4f matrix, float angle) {
        return this.rotate(matrix, angle * ((float)Math.PI / 180));
    }
}

