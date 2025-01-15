SELECT u.username, GROUP_CONCAT(DISTINCT (week(t.date_created))) AS Week, (SELECT DATE(SUBDATE(t.date_created, weekday(t.date_created)))) AS WeekStarts,
       count(*) AS SentencesTranslated
FROM translated_sentence t
         CROSS JOIN batch_details bd on t.batch_details_id = bd.batch_details_id
         CROSS JOIN users u on u.user_id = bd.translated_by WHERE t.deletion_status = 0 AND bd.deletion_status = 0 GROUP BY u.username, week(t.date_created) ORDER BY WEEK(t.date_created);