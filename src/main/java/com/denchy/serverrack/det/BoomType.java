package com.denchy.serverrack.det;

/**
 * Detonation scripts. First five are generic, next five are written for the
 * "Rockstar office server room goes critical" scene.
 */
public enum BoomType {
    SHOCKWAVE("Шоквейв", "Волна от эпицентра, блоки сносит наружу"),
    FUSE("Фитиль", "Змейка цепных подрывов через весь объём"),
    PANCAKE("Панкейк", "Этажи складываются снизу вверх, пыль столбом"),
    SHRAPNEL("Шрапнель", "Весь регион разлетается в хаосе за считанные секунды"),
    TWO_STAGE("Двухфазный", "Сначала пожар и дым, потом секционная детонация"),
    MELTDOWN("Расплавление ядра", "Серверная искрит, потом грибовидный взрыв"),
    FLAK("Залп", "Серия зенитных разрывов случайных секций"),
    IMPLODE("Имплозия", "Блоки втягивает к центру, финал - вспышка"),
    SWEEP("Срез", "Плоскость опустошения скользит с края до края"),
    RAIN("Артобстрел", "Снаряды сверху, каждый забирает свой столб блоков");

    private final String ruName;
    private final String ruHint;

    BoomType(String ruName, String ruHint) {
        this.ruName = ruName;
        this.ruHint = ruHint;
    }

    public String ruName() { return ruName; }
    public String ruHint() { return ruHint; }

    public BoomType next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static BoomType byIndex(int i) {
        BoomType[] v = values();
        return v[((i % v.length) + v.length) % v.length];
    }
}
