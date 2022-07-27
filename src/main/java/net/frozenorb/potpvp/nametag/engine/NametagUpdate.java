package net.frozenorb.potpvp.nametag.engine;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter @AllArgsConstructor @RequiredArgsConstructor
public final class NametagUpdate {

    private final UUID toRefresh;
    private UUID refreshFor;
}
