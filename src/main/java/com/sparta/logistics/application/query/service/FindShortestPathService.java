package com.sparta.logistics.application.query.service;

import com.sparta.logistics.application.graph.HubGraphManager;
import com.sparta.logistics.application.port.ShortestPathCache;
import com.sparta.logistics.application.query.dto.ShortestPathResponse;
import com.sparta.logistics.application.query.usecase.FindShortestPathUseCase;
import com.sparta.logistics.domain.graph.HubGraph;
import com.sparta.logistics.domain.graph.PathFinder;
import com.sparta.logistics.domain.model.ShortestPath;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindShortestPathService implements FindShortestPathUseCase {

    private final HubGraphManager hubGraphManager;
    private final PathFinder pathFinder;
    private final ShortestPathCache shortestPathCache;

    @Value("${performance.shortest-path-cache.enabled:true}")
    private boolean cacheEnabled;

    @Override
    public ShortestPathResponse findShortestPath(
            UUID fromHubId,
            UUID toHubId
    ){
        long graphVersion = hubGraphManager.getLocalGraphVersion();

        if(cacheEnabled) {
            Optional<ShortestPath> cached = shortestPathCache.get(graphVersion, fromHubId, toHubId);

            if(cached.isPresent()){
                return ShortestPathResponse.from(cached.get());
            }
        }

        HubGraph hubGraph = hubGraphManager.getGraph();

        ShortestPath shortestPath = pathFinder.findShortestPath(
                hubGraph,
                fromHubId,
                toHubId
        );

        //캐시 적재
        if (cacheEnabled) {
            shortestPathCache.put(
                    graphVersion,
                    fromHubId,
                    toHubId,
                    shortestPath
            );
        }

        return ShortestPathResponse.from(shortestPath);
    }
}
