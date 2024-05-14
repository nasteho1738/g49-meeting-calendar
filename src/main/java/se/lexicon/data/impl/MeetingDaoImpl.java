package se.lexicon.data.impl;

import se.lexicon.data.MeetingDao;
import se.lexicon.data.db.MeetingCalendarDBConnection;
import se.lexicon.exception.MySQLException;
import se.lexicon.model.Meeting;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//todo Implement methods

public class MeetingDaoImpl implements MeetingDao {
    private Connection connection;
    private int id;

    public MeetingDaoImpl(Connection connection) {
        this.connection = MeetingCalendarDBConnection.getConnection();
    }

    @Override
    public Meeting createMeeting(Meeting meeting) {
        String query = "insert into meeting(title, description, start_time, end_time, calender_id) values(?,?,?,?,?)";
        try(
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ){
            preparedStatement.setString(1, meeting.getTitle());
            preparedStatement.setString(2, meeting.getDescription());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(meeting.getStartTime()));
            preparedStatement.setTimestamp(4, Timestamp.valueOf(meeting.getEndTime()));
            preparedStatement.setInt(5, meeting.getCalendar().getId());

            int affectedrows = preparedStatement.executeUpdate();
            if(affectedrows == 0) {
                throw new MySQLException("Creating meeting failed, no rows effected.");
            }

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if(generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                meeting.setId(id);
            }else {
                throw new MySQLException("Creating meeting failed, no Id obtained.");
            }
            return meeting;
        }catch (SQLException e) {
            throw new MySQLException("Error occurred while creating meeting: " + meeting.getTitle(), e);

        }
    }

    @Override
    public Optional<Meeting> findById(int id) {
        String query = "select * from meetings where id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query);
        ){
            preparedStatement.setInt(1,id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int meetingId = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                java.sql.Timestamp startTime = resultSet.getTimestamp("start_time");
                java.sql.Timestamp endTime = resultSet.getTimestamp("end_time");
                int calendarId = resultSet.getInt("calendar_id");

                Meeting meeting = new Meeting(title, description, startTime, endTime, calendarId);
                meeting.setId(meetingId);

                return Optional.of(meeting);
            } else {
                return Optional.empty();
            }
        } catch (SQLException e){
                throw new MySQLException("Error occurred while finding the meeting by id: " + id, e);
        }
    }

    @Override
    public List<Meeting> findAllMeetingsByCalendarId(int calendarId) {
        String query = "SELECT * FROM meetings WHERE calendar_id = ?";

        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ) {
            preparedStatement.setInt(1, calendarId);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<Meeting> meetings = new ArrayList<>();

            while (resultSet.next()) {
                int meetingId = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                LocalDateTime startTime = resultSet.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime endTime = resultSet.getTimestamp("end_time").toLocalDateTime();
                int id = resultSet.getInt("calendar_id");

                Meeting meeting = new Meeting(title, description, startTime, endTime, id);
                meeting.setId(meetingId);

                meetings.add(meeting);
            }

            return meetings;

        } catch (SQLException e) {
            throw new MySQLException("Error occurred while finding all meetings by calendar ID: " + calendarId, e);
        }
    }

    @Override
    public boolean deleteMeeting(int meetingId) {
        String query = "Delete from meetings where id =?";
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
        ) {
            preparedStatement.setInt(1, meetingId);
            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new MySQLException("Error occurred while deleting the meeting by ID: " + meetingId, e);
        }
    }
}
