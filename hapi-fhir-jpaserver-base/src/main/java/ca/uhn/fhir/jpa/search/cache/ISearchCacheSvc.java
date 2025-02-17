package ca.uhn.fhir.jpa.search.cache;

import ca.uhn.fhir.jpa.entity.Search;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

public interface ISearchCacheSvc {

	/**
	 * Places a new search of some sort in the cache, or updates an existing search. The search passed in is guaranteed to have
	 * a {@link Search#getUuid() UUID} so that is a good candidate for consistent identification.
	 *
	 * @param theSearch The search to store
	 * @return Returns a copy of the search as it was saved. Callers should use the returned Search object for any further processing.
	 */
	Search save(Search theSearch);

	/**
	 * Fetch a search using its UUID. The search should be fully loaded when it is returned (i.e. includes are fetched, so that access to its
	 * fields will not cause database errors if the current tranaction scope ends.
	 *
	 * @param theUuid The search UUID
	 * @return The search if it exists
	 */
	Optional<Search> fetchByUuid(String theUuid);

	/**
	 * TODO: this is perhaps an inappropriate responsibility for this service
	 *
	 * <p>
	 * This method marks a search as in progress, but should only allow exactly one call to do so across the cluster. This
	 * is done so that if two client threads request the next page at the exact same time (which is unlikely but not
	 * impossible) only one will actually proceed to load the next results and the other will just wait for them
	 * to arrive.
	 *
	 * @param theSearch The search to mark
	 * @return This method should return an empty optional if the search was not marked (meaning that another thread
	 * succeeded in marking it). If the search doesn't exist or some other error occurred, an exception will be thrown
	 * instead of {@link Optional#empty()}
	 */
	Optional<Search> tryToMarkSearchAsInProgress(Search theSearch);

	/**
	 * Look for any existing searches matching the given resource type and query string.
	 * <p>
	 * This method is allowed to perofrm a "best effort" return, so it can return searches that don't match the query string exactly, or
	 * which have a created timestamp before <code>theCreatedAfter</code> date. The caller is responsible for removing
	 * any inappropriate Searches and picking the most relevant one.
	 * </p>
	 *
	 * @param theResourceType The resource type of the search. Results MUST match this type
	 * @param theQueryString  The query string. Results SHOULD match this type
	 * @param theQueryStringHash The query string hash. Results SHOULD match this type
	 * @param theCreatedAfter Results SHOULD not include any searches created before this cutoff timestamp
	 * @return A collection of candidate searches
	 */
	Collection<Search> findCandidatesForReuse(String theResourceType, String theQueryString, int theQueryStringHash, Date theCreatedAfter);

	/**
	 * Mark a search as having been "last used" at the given time. This method may (and probably should) be implemented
	 * to work asynchronously in order to avoid hammering the database if the search gets reused many times.
	 *
	 * @param theSearch The search
	 * @param theDate   The "last returned" timestamp
	 */
	void updateSearchLastReturned(Search theSearch, Date theDate);

	/**
	 * This is mostly public for unit tests
	 */
	void flushLastUpdated();

	/**
	 * This method will be called periodically to delete stale searches. Implementations are not required to do anything
	 * if they have some other mechanism for expiring stale results other than manually looking for them
	 * and deleting them.
	 */
	void pollForStaleSearchesAndDeleteThem();
}
