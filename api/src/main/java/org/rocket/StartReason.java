package org.rocket;

/**
 * StartReason gives context to different kind of start.
 */
public enum StartReason {
    /**
     * A cold normal start.
     */
    NORMAL,

    /**
     * A hot restart.
     */
    RESTART,
}
