package se.lexicon.data;

import se.lexicon.model.Meeting;

import java.util.List;
import java.util.Optional;

public interface MeetingDao {

    Meeting createMeeting(Meeting meeting);

    Optional<Meeting> findById(int id);

    List<Meeting> findAllMeetingsByCalendarId(int calendarId);

    boolean deleteMeeting(int meetingId);
    //Add more methods as needed
}
