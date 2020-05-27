package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.domain.paginated.Mails
import fr.fabienhebuterne.marketplace.storage.MailsRepository

class MailsService(mailsRepository: MailsRepository) : PaginationService<Mails>(mailsRepository)