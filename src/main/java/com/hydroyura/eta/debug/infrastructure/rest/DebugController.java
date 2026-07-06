package com.hydroyura.eta.debug.infrastructure.rest;

import com.hydroyura.eta.shared.api.SnapshotProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO: remove entire module when switching to JPA/PostgreSQL (use Actuator or DB tools instead)
 */
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final List<SnapshotProvider> providers;

    @GetMapping
    public Map<String, Object> dump() {
        var result = new LinkedHashMap<String, Object>();
        for (var provider : providers) {
            var name = provider.getClass().getSimpleName()
                    .replace("InMemory", "")
                    .replace("Repository", "");
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            result.put(name, dumpMap(provider.snapshot()));
        }
        return result;
    }

    private Map<String, Object> dumpMap(Map<?, ?> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }
}
