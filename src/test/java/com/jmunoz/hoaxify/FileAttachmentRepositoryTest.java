package com.jmunoz.hoaxify;

import com.jmunoz.hoaxify.file.FileAttachment;
import com.jmunoz.hoaxify.file.FileAttachmentRepository;
import com.jmunoz.hoaxify.hoax.Hoax;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class FileAttachmentRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    // Todos estos tests son para eliminar adjuntos no asociados a ningún hoax.
    // Si el fichero lleva una hora creado y no tiene un hoax asociado, se eliminará.

    private FileAttachment getOneHourOldFileAttachment() {
        // Un milisegundo más de una hora
        Date date = new Date(System.currentTimeMillis() - (60*60*1000) - 1);
        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setDate(date);
        return fileAttachment;
    }

    private FileAttachment getOldFileAttachmentWithHoax(Hoax hoax) {
        FileAttachment fileAttachment = getOneHourOldFileAttachment();
        fileAttachment.setHoax(hoax);
        return fileAttachment;
    }

    @Test
    void findByDateBeforeAndHoaxIsNull_whenAttachmentsDateOlderThanOneHour_returnsAll() {
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());

        Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(3);
    }

    @Test
    void findByDateBeforeAndHoaxIsNull_whenAttachmentsDateOlderThanOneHourButHaveHoax_returnsNone() {
        Hoax hoax1 = testEntityManager.persist(TestUtil.createValidHoax());
        Hoax hoax2 = testEntityManager.persist(TestUtil.createValidHoax());
        Hoax hoax3 = testEntityManager.persist(TestUtil.createValidHoax());

        testEntityManager.persist(getOldFileAttachmentWithHoax(hoax1));
        testEntityManager.persist(getOldFileAttachmentWithHoax(hoax2));
        testEntityManager.persist(getOldFileAttachmentWithHoax(hoax3));

        Date oneHourAgo = new Date(System.currentTimeMillis() - (60*60*1000));
        List<FileAttachment> attachments = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);
        assertThat(attachments.size()).isEqualTo(0);
    }
}
