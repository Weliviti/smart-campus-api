package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;

/**
 * Manages campus rooms at {@code /api/v1/rooms}.
 *
 * <p>Business rule: a room with sensors assigned to it cannot be deleted
 * (handled by {@link RoomNotEmptyException}).</p>
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoom {

    private final DataStore store = DataStore.getInstance();

    /* ----------  GET /rooms  ---------- */

    @GET
    public Collection<Room> listRooms() {
        return store.listRooms();
    }

    /* ----------  GET /rooms/{roomId}  ---------- */

    @GET
    @Path("/{roomId}")
    public Room getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            throw new NotFoundException("Room '" + roomId + "' not found.");
        }
        return room;
    }

    /* ----------  POST /rooms  ---------- */

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room == null || room.getId() == null || room.getId().isEmpty()) {
            throw new WebApplicationException(
                    "Room id is required.", Response.Status.BAD_REQUEST);
        }
        if (store.roomExists(room.getId())) {
            throw new WebApplicationException(
                    "Room '" + room.getId() + "' already exists.",
                    Response.Status.CONFLICT);
        }
        store.addRoom(room);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(room.getId())
                .build();
        return Response.created(location).entity(room).build();
    }

    /* ----------  DELETE /rooms/{roomId}  ---------- */

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            // Idempotent DELETE: 404 if it was never there.
            throw new NotFoundException("Room '" + roomId + "' not found.");
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId, room.getSensorIds().size());
        }
        store.removeRoom(roomId);
        return Response.noContent().build();
    }
}
