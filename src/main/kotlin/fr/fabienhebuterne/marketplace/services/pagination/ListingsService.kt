package fr.fabienhebuterne.marketplace.services.pagination

import fr.fabienhebuterne.marketplace.domain.paginated.Listings
import fr.fabienhebuterne.marketplace.storage.ListingsRepository

class ListingsService(listingsRepository: ListingsRepository) : PaginationService<Listings>(listingsRepository)