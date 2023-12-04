package com.example.autoscheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/plans")
public class PlanDataController {

    @Autowired
    private PlanDataRepositoryStudent planDataRepositoryUni;

    // Define the mapping of 'type' to its corresponding code
    private Map<String, Integer> typeCodeMap = new HashMap<>();

    public PlanDataController() {
        // 생성자에서 초기화
        typeCodeMap.put("공부", 1);
        typeCodeMap.put("저녁", 2);
        typeCodeMap.put("점심식사", 2);
        typeCodeMap.put("아침식사", 2);
        typeCodeMap.put("아침", 2);
        typeCodeMap.put("점심", 2);
        typeCodeMap.put("휴식", 3);
        typeCodeMap.put("게임", 3);
        typeCodeMap.put("여가", 4);
        typeCodeMap.put("수면", 5);
        typeCodeMap.put("취침", 5);
        typeCodeMap.put("숙면", 5);
        typeCodeMap.put("운동", 6);
    }

    @GetMapping("/university-students")
    public List<Integer> getUniversityStudents() {
        List<PlanData> uniStudents = planDataRepositoryUni.findByStudentIs("대학생");
        return uniStudents.stream()
                .map(PlanData::getPerson)
                .collect(Collectors.toList());
    }

    @GetMapping("/peee")
    public List<TimeCodeDto> getPersonsStartEndWithTypeCode() {
        List<PlanData> uniStudents = planDataRepositoryUni.findByStudentIs("대학생");
        List<Integer> personValues = uniStudents.stream()
                .map(PlanData::getPerson)
                .collect(Collectors.toList());

        // Find PlanData by 'person' in the given list
        List<PlanData> matchingData = planDataRepositoryUni.findByPersonIn(personValues);

        // Extract 'typeCode', 'sex', 'start', 'end', and 'person' values and create a list of TimeCodeDto
        List<TimeCodeDto> timeCodeList = matchingData.stream()
                .collect(Collectors.groupingBy(PlanData::getPerson)) // Group by 'person'
                .entrySet().stream()
                .map(entry -> {
                    int person = entry.getKey();
                    List<PlanData> personData = entry.getValue();

                    // Combine time codes for the same person
                    String combinedTimeCode = combineTimeCodes(personData);

                    // Create TimeCodeDto
                    return new TimeCodeDto(person, personData.get(0).getSex(), combinedTimeCode);
                })
                .collect(Collectors.toList());

        return timeCodeList;
    }

    // Existing methods...

    // Method to combine time codes for the same person
    private String combineTimeCodes(List<PlanData> personData) {
        // Initialize a char array for the combined time code with 48 '0' characters
        char[] combinedTimeCode = new char[48];
        for (int i = 0; i < combinedTimeCode.length; i++) {
            combinedTimeCode[i] = '0';
        }

        // Iterate through each PlanData and update the combined time code
        for (PlanData planData : personData) {
            String timeCode = createTypeCode(planData);
            for (int i = 0; i < combinedTimeCode.length; i++) {
                // Combine time codes by taking the maximum value at each position
                combinedTimeCode[i] = (char) Math.max(combinedTimeCode[i], timeCode.charAt(i));
            }
        }

        // Convert the char array to a String
        return new String(combinedTimeCode);
    }

    @GetMapping("/school-students")
    public List<Integer> getSchoolStudents() {
        List<PlanData> middleSchoolStudents = planDataRepositoryUni.findByStudentIs("중학생");
        List<PlanData> highSchoolStudents = planDataRepositoryUni.findByStudentEquals("고등학생");

        // Combine the two lists (You may need to handle duplicates based on your requirements)
        middleSchoolStudents.addAll(highSchoolStudents);

        List<Integer> personValues = middleSchoolStudents.stream()
                .map(PlanData::getPerson)
                .collect(Collectors.toList());

        return personValues;
    }

    // Method to create the time code for a given PlanData
    private String createTypeCode(PlanData planData) {
        // Extract the 'start' and 'end' times from the PlanData
        LocalDateTime startTime = planData.getStart();
        LocalDateTime endTime = planData.getEnd();

        // Initialize a char array for the time code with 48 '0' characters
        char[] timeCode = new char[48];
        for (int i = 0; i < timeCode.length; i++) {
            timeCode[i] = '0';
        }

        // Set the type code in the time code array based on the 'start' and 'end' times
        int typeCode = typeCodeMap.getOrDefault(planData.getType(), 0);

        // Check if 'start' is greater than 'end', indicating a span across midnight
        if (startTime.isAfter(endTime)) {
            // Calculate the time code for the first part of the span
            int startSlot = getSlotIndex(startTime);
            int endSlot = 47; // Index of the last slot

            for (int i = startSlot; i <= endSlot; i++) {
                timeCode[i] = Character.forDigit(typeCode, 10);
            }

            // Calculate the time code for the second part of the span
            startSlot = 0; // Index of the first slot
            endSlot = getSlotIndex(endTime);

            for (int i = startSlot; i <= endSlot; i++) {
                timeCode[i] = Character.forDigit(typeCode, 10);
            }
        } else {
            // Calculate the time code for the regular case
            int startSlot = getSlotIndex(startTime);
            int endSlot = getSlotIndex(endTime);

            for (int i = startSlot; i <= endSlot; i++) {
                timeCode[i] = Character.forDigit(typeCode, 10);
            }
        }


        // Convert the char array to a String
        return new String(timeCode);
    }

    // Method to calculate the time slot index (30-minute slots) for a given time
    private int getSlotIndex(LocalDateTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();
        return (hour * 2) + (minute / 30);
    }


}