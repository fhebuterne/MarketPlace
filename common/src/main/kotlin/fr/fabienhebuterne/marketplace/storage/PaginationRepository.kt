package fr.fabienhebuterne.marketplace.storage

import fr.fabienhebuterne.marketplace.domain.paginated.Paginated

interface PaginationRepository<T : Paginated> : Repository<T>