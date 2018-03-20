package com.mv.data.panda.model;

/**
 * author: houying
 * date  : 18-1-10
 * desc  :
 */
public enum ProjectState {
    INIT(0),
    UPLOAD(1),
    RUNNING(2),
    STOP(3),
    DISABLE(4);

    private int v;

    ProjectState(int v) {
        this.v = v;
    }

    public int value() {
        return v;
    }

    public static ProjectState valueOf(int v) {
        for(ProjectState state: ProjectState.values()) {
            if (state.value() == v) {
                return state;
            }
        }
        throw new IllegalArgumentException("无法识别的工程状态");
    }
}
