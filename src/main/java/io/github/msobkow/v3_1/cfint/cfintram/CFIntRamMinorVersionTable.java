
// Description: Java 25 in-memory RAM DbIO implementation for MinorVersion.

/*
 *	io.github.msobkow.CFInt
 *
 *	Copyright (c) 2016-2026 Mark Stephen Sobkow
 *	
 *	Mark's Code Fractal 3.1 CFInt - Internet Essentials
 *	
 *	This file is part of Mark's Code Fractal CFInt.
 *	
 *	Mark's Code Fractal CFInt is available under dual commercial license from
 *	Mark Stephen Sobkow, or under the terms of the GNU Library General Public License,
 *	Version 3 or later.
 *	
 *	Mark's Code Fractal CFInt is free software: you can redistribute it and/or
 *	modify it under the terms of the GNU Library General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *	
 *	Mark's Code Fractal CFInt is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *	
 *	You should have received a copy of the GNU Library General Public License
 *	along with Mark's Code Fractal CFInt.  If not, see <https://www.gnu.org/licenses/>.
 *	
 *	If you wish to modify and use this code without publishing your changes in order to
 *	tie it to proprietary code, please contact Mark Stephen Sobkow
 *	for a commercial license at mark.sobkow@gmail.com
 *	
 */

package io.github.msobkow.v3_1.cfint.cfintram;

import java.math.*;
import java.sql.*;
import java.text.*;
import java.time.*;
import java.util.*;
import org.apache.commons.codec.binary.Base64;
import io.github.msobkow.v3_1.cflib.*;
import io.github.msobkow.v3_1.cflib.dbutil.*;

import io.github.msobkow.v3_1.cfsec.cfsec.*;
import io.github.msobkow.v3_1.cfint.cfint.*;
import io.github.msobkow.v3_1.cfsec.cfsec.buff.*;
import io.github.msobkow.v3_1.cfint.cfint.buff.*;
import io.github.msobkow.v3_1.cfsec.cfsecobj.*;
import io.github.msobkow.v3_1.cfint.cfintobj.*;

/*
 *	CFIntRamMinorVersionTable in-memory RAM DbIO implementation
 *	for MinorVersion.
 */
public class CFIntRamMinorVersionTable
	implements ICFIntMinorVersionTable
{
	private ICFIntSchema schema;
	private Map< CFLibDbKeyHash256,
				CFIntBuffMinorVersion > dictByPKey
		= new HashMap< CFLibDbKeyHash256,
				CFIntBuffMinorVersion >();
	private Map< CFIntBuffMinorVersionByTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffMinorVersion >> dictByTenantIdx
		= new HashMap< CFIntBuffMinorVersionByTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffMinorVersion >>();
	private Map< CFIntBuffMinorVersionByMajorVerIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffMinorVersion >> dictByMajorVerIdx
		= new HashMap< CFIntBuffMinorVersionByMajorVerIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffMinorVersion >>();
	private Map< CFIntBuffMinorVersionByNameIdxKey,
			CFIntBuffMinorVersion > dictByNameIdx
		= new HashMap< CFIntBuffMinorVersionByNameIdxKey,
			CFIntBuffMinorVersion >();

	public CFIntRamMinorVersionTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public CFIntBuffMinorVersion ensureRec(ICFIntMinorVersion rec) {
		if (rec == null) {
			return( null );
		}
		else {
			int classCode = rec.getClassCode();
			if (classCode == ICFIntMinorVersion.CLASS_CODE) {
				return( ((CFIntBuffMinorVersionDefaultFactory)(schema.getFactoryMinorVersion())).ensureRec(rec) );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), "ensureRec", 1, "rec", "Not " + Integer.toString(classCode));
			}
		}
	}

	public ICFIntMinorVersion createMinorVersion( ICFSecAuthorization Authorization,
		ICFIntMinorVersion iBuff )
	{
		final String S_ProcName = "createMinorVersion";
		
		CFIntBuffMinorVersion Buff = ensureRec(iBuff);
		CFLibDbKeyHash256 pkey;
		pkey = schema.nextMinorVersionIdGen();
		Buff.setRequiredId( pkey );
		CFIntBuffMinorVersionByTenantIdxKey keyTenantIdx = (CFIntBuffMinorVersionByTenantIdxKey)schema.getFactoryMinorVersion().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffMinorVersionByMajorVerIdxKey keyMajorVerIdx = (CFIntBuffMinorVersionByMajorVerIdxKey)schema.getFactoryMinorVersion().newByMajorVerIdxKey();
		keyMajorVerIdx.setRequiredMajorVersionId( Buff.getRequiredMajorVersionId() );

		CFIntBuffMinorVersionByNameIdxKey keyNameIdx = (CFIntBuffMinorVersionByNameIdxKey)schema.getFactoryMinorVersion().newByNameIdxKey();
		keyNameIdx.setRequiredMajorVersionId( Buff.getRequiredMajorVersionId() );
		keyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Validate unique indexes

		if( dictByPKey.containsKey( pkey ) ) {
			throw new CFLibPrimaryKeyNotNewException( getClass(), S_ProcName, pkey );
		}

		if( dictByNameIdx.containsKey( keyNameIdx ) ) {
			throw new CFLibUniqueIndexViolationException( getClass(),
				S_ProcName,
				"MinorVersionNameIdx",
				"MinorVersionNameIdx",
				keyNameIdx );
		}

		// Validate foreign keys

		{
			boolean allNull = true;
			allNull = false;
			if( ! allNull ) {
				if( null == schema.getTableTenant().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTenantId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						S_ProcName,
						"Owner",
						"Tenant",
						"Tenant",
						null );
				}
			}
		}

		{
			boolean allNull = true;
			allNull = false;
			if( ! allNull ) {
				if( null == schema.getTableMajorVersion().readDerivedByIdIdx( Authorization,
						Buff.getRequiredMajorVersionId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						S_ProcName,
						"Container",
						"ParentMajorVersion",
						"MajorVersion",
						null );
				}
			}
		}

		// Proceed with adding the new record

		dictByPKey.put( pkey, Buff );

		Map< CFLibDbKeyHash256, CFIntBuffMinorVersion > subdictTenantIdx;
		if( dictByTenantIdx.containsKey( keyTenantIdx ) ) {
			subdictTenantIdx = dictByTenantIdx.get( keyTenantIdx );
		}
		else {
			subdictTenantIdx = new HashMap< CFLibDbKeyHash256, CFIntBuffMinorVersion >();
			dictByTenantIdx.put( keyTenantIdx, subdictTenantIdx );
		}
		subdictTenantIdx.put( pkey, Buff );

		Map< CFLibDbKeyHash256, CFIntBuffMinorVersion > subdictMajorVerIdx;
		if( dictByMajorVerIdx.containsKey( keyMajorVerIdx ) ) {
			subdictMajorVerIdx = dictByMajorVerIdx.get( keyMajorVerIdx );
		}
		else {
			subdictMajorVerIdx = new HashMap< CFLibDbKeyHash256, CFIntBuffMinorVersion >();
			dictByMajorVerIdx.put( keyMajorVerIdx, subdictMajorVerIdx );
		}
		subdictMajorVerIdx.put( pkey, Buff );

		dictByNameIdx.put( keyNameIdx, Buff );

		if (Buff == null) {
			return( null );
		}
		else {
			int classCode = Buff.getClassCode();
			if (classCode == ICFIntMinorVersion.CLASS_CODE) {
				CFIntBuffMinorVersion retbuff = ((CFIntBuffMinorVersion)(schema.getFactoryMinorVersion().newRec()));
				retbuff.set(Buff);
				return( retbuff );
			}
			else {
				throw new CFLibUnsupportedClassException(getClass(), S_ProcName, 0, "-create-buff-cloning-", "Not " + Integer.toString(classCode));
			}
		}
	}

	public ICFIntMinorVersion readDerived( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readDerived";
		ICFIntMinorVersion buff;
		if( dictByPKey.containsKey( PKey ) ) {
			buff = dictByPKey.get( PKey );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntMinorVersion lockDerived( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readDerived";
		ICFIntMinorVersion buff;
		if( dictByPKey.containsKey( PKey ) ) {
			buff = dictByPKey.get( PKey );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntMinorVersion[] readAllDerived( ICFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamMinorVersion.readAllDerived";
		ICFIntMinorVersion[] retList = new ICFIntMinorVersion[ dictByPKey.values().size() ];
		Iterator< ICFIntMinorVersion > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	public ICFIntMinorVersion[] readDerivedByTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readDerivedByTenantIdx";
		CFIntBuffMinorVersionByTenantIdxKey key = (CFIntBuffMinorVersionByTenantIdxKey)schema.getFactoryMinorVersion().newByTenantIdxKey();
		key.setRequiredTenantId( TenantId );

		ICFIntMinorVersion[] recArray;
		if( dictByTenantIdx.containsKey( key ) ) {
			Map< CFLibDbKeyHash256, CFIntBuffMinorVersion > subdictTenantIdx
				= dictByTenantIdx.get( key );
			recArray = new ICFIntMinorVersion[ subdictTenantIdx.size() ];
			Iterator< ICFIntMinorVersion > iter = subdictTenantIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFLibDbKeyHash256, CFIntBuffMinorVersion > subdictTenantIdx
				= new HashMap< CFLibDbKeyHash256, CFIntBuffMinorVersion >();
			dictByTenantIdx.put( key, subdictTenantIdx );
			recArray = new ICFIntMinorVersion[0];
		}
		return( recArray );
	}

	public ICFIntMinorVersion[] readDerivedByMajorVerIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 MajorVersionId )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readDerivedByMajorVerIdx";
		CFIntBuffMinorVersionByMajorVerIdxKey key = (CFIntBuffMinorVersionByMajorVerIdxKey)schema.getFactoryMinorVersion().newByMajorVerIdxKey();
		key.setRequiredMajorVersionId( MajorVersionId );

		ICFIntMinorVersion[] recArray;
		if( dictByMajorVerIdx.containsKey( key ) ) {
			Map< CFLibDbKeyHash256, CFIntBuffMinorVersion > subdictMajorVerIdx
				= dictByMajorVerIdx.get( key );
			recArray = new ICFIntMinorVersion[ subdictMajorVerIdx.size() ];
			Iterator< ICFIntMinorVersion > iter = subdictMajorVerIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFLibDbKeyHash256, CFIntBuffMinorVersion > subdictMajorVerIdx
				= new HashMap< CFLibDbKeyHash256, CFIntBuffMinorVersion >();
			dictByMajorVerIdx.put( key, subdictMajorVerIdx );
			recArray = new ICFIntMinorVersion[0];
		}
		return( recArray );
	}

	public ICFIntMinorVersion readDerivedByNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 MajorVersionId,
		String Name )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readDerivedByNameIdx";
		CFIntBuffMinorVersionByNameIdxKey key = (CFIntBuffMinorVersionByNameIdxKey)schema.getFactoryMinorVersion().newByNameIdxKey();
		key.setRequiredMajorVersionId( MajorVersionId );
		key.setRequiredName( Name );

		ICFIntMinorVersion buff;
		if( dictByNameIdx.containsKey( key ) ) {
			buff = dictByNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntMinorVersion readDerivedByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readDerivedByIdIdx() ";
		ICFIntMinorVersion buff;
		if( dictByPKey.containsKey( Id ) ) {
			buff = dictByPKey.get( Id );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntMinorVersion readBuff( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readBuff";
		ICFIntMinorVersion buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntMinorVersion.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	public ICFIntMinorVersion lockBuff( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "lockBuff";
		ICFIntMinorVersion buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( buff.getClassCode() != ICFIntMinorVersion.CLASS_CODE ) ) {
			buff = null;
		}
		return( buff );
	}

	public ICFIntMinorVersion[] readAllBuff( ICFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readAllBuff";
		ICFIntMinorVersion buff;
		ArrayList<ICFIntMinorVersion> filteredList = new ArrayList<ICFIntMinorVersion>();
		ICFIntMinorVersion[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntMinorVersion.CLASS_CODE ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new ICFIntMinorVersion[0] ) );
	}

	public ICFIntMinorVersion readBuffByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readBuffByIdIdx() ";
		ICFIntMinorVersion buff = readDerivedByIdIdx( Authorization,
			Id );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntMinorVersion.CLASS_CODE ) ) {
			return( (ICFIntMinorVersion)buff );
		}
		else {
			return( null );
		}
	}

	public ICFIntMinorVersion[] readBuffByTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readBuffByTenantIdx() ";
		ICFIntMinorVersion buff;
		ArrayList<ICFIntMinorVersion> filteredList = new ArrayList<ICFIntMinorVersion>();
		ICFIntMinorVersion[] buffList = readDerivedByTenantIdx( Authorization,
			TenantId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntMinorVersion.CLASS_CODE ) ) {
				filteredList.add( (ICFIntMinorVersion)buff );
			}
		}
		return( filteredList.toArray( new ICFIntMinorVersion[0] ) );
	}

	public ICFIntMinorVersion[] readBuffByMajorVerIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 MajorVersionId )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readBuffByMajorVerIdx() ";
		ICFIntMinorVersion buff;
		ArrayList<ICFIntMinorVersion> filteredList = new ArrayList<ICFIntMinorVersion>();
		ICFIntMinorVersion[] buffList = readDerivedByMajorVerIdx( Authorization,
			MajorVersionId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && ( buff.getClassCode() == ICFIntMinorVersion.CLASS_CODE ) ) {
				filteredList.add( (ICFIntMinorVersion)buff );
			}
		}
		return( filteredList.toArray( new ICFIntMinorVersion[0] ) );
	}

	public ICFIntMinorVersion readBuffByNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 MajorVersionId,
		String Name )
	{
		final String S_ProcName = "CFIntRamMinorVersion.readBuffByNameIdx() ";
		ICFIntMinorVersion buff = readDerivedByNameIdx( Authorization,
			MajorVersionId,
			Name );
		if( ( buff != null ) && ( buff.getClassCode() == ICFIntMinorVersion.CLASS_CODE ) ) {
			return( (ICFIntMinorVersion)buff );
		}
		else {
			return( null );
		}
	}

	public ICFIntMinorVersion updateMinorVersion( ICFSecAuthorization Authorization,
		ICFIntMinorVersion Buff )
	{
		CFLibDbKeyHash256 pkey = Buff.getPKey();
		ICFIntMinorVersion existing = dictByPKey.get( pkey );
		if( existing == null ) {
			throw new CFLibStaleCacheDetectedException( getClass(),
				"updateMinorVersion",
				"Existing record not found",
				"MinorVersion",
				pkey );
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() ) {
			throw new CFLibCollisionDetectedException( getClass(),
				"updateMinorVersion",
				pkey );
		}
		Buff.setRequiredRevision( Buff.getRequiredRevision() + 1 );
		CFIntBuffMinorVersionByTenantIdxKey existingKeyTenantIdx = (CFIntBuffMinorVersionByTenantIdxKey)schema.getFactoryMinorVersion().newByTenantIdxKey();
		existingKeyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffMinorVersionByTenantIdxKey newKeyTenantIdx = (CFIntBuffMinorVersionByTenantIdxKey)schema.getFactoryMinorVersion().newByTenantIdxKey();
		newKeyTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffMinorVersionByMajorVerIdxKey existingKeyMajorVerIdx = (CFIntBuffMinorVersionByMajorVerIdxKey)schema.getFactoryMinorVersion().newByMajorVerIdxKey();
		existingKeyMajorVerIdx.setRequiredMajorVersionId( existing.getRequiredMajorVersionId() );

		CFIntBuffMinorVersionByMajorVerIdxKey newKeyMajorVerIdx = (CFIntBuffMinorVersionByMajorVerIdxKey)schema.getFactoryMinorVersion().newByMajorVerIdxKey();
		newKeyMajorVerIdx.setRequiredMajorVersionId( Buff.getRequiredMajorVersionId() );

		CFIntBuffMinorVersionByNameIdxKey existingKeyNameIdx = (CFIntBuffMinorVersionByNameIdxKey)schema.getFactoryMinorVersion().newByNameIdxKey();
		existingKeyNameIdx.setRequiredMajorVersionId( existing.getRequiredMajorVersionId() );
		existingKeyNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntBuffMinorVersionByNameIdxKey newKeyNameIdx = (CFIntBuffMinorVersionByNameIdxKey)schema.getFactoryMinorVersion().newByNameIdxKey();
		newKeyNameIdx.setRequiredMajorVersionId( Buff.getRequiredMajorVersionId() );
		newKeyNameIdx.setRequiredName( Buff.getRequiredName() );

		// Check unique indexes

		if( ! existingKeyNameIdx.equals( newKeyNameIdx ) ) {
			if( dictByNameIdx.containsKey( newKeyNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateMinorVersion",
					"MinorVersionNameIdx",
					"MinorVersionNameIdx",
					newKeyNameIdx );
			}
		}

		// Validate foreign keys

		{
			boolean allNull = true;

			if( allNull ) {
				if( null == schema.getTableTenant().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTenantId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						"updateMinorVersion",
						"Owner",
						"Tenant",
						"Tenant",
						null );
				}
			}
		}

		{
			boolean allNull = true;

			if( allNull ) {
				if( null == schema.getTableMajorVersion().readDerivedByIdIdx( Authorization,
						Buff.getRequiredMajorVersionId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						"updateMinorVersion",
						"Container",
						"ParentMajorVersion",
						"MajorVersion",
						null );
				}
			}
		}

		// Update is valid

		Map< CFLibDbKeyHash256, CFIntBuffMinorVersion > subdict;

		dictByPKey.remove( pkey );
		dictByPKey.put( pkey, Buff );

		subdict = dictByTenantIdx.get( existingKeyTenantIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByTenantIdx.containsKey( newKeyTenantIdx ) ) {
			subdict = dictByTenantIdx.get( newKeyTenantIdx );
		}
		else {
			subdict = new HashMap< CFLibDbKeyHash256, CFIntBuffMinorVersion >();
			dictByTenantIdx.put( newKeyTenantIdx, subdict );
		}
		subdict.put( pkey, Buff );

		subdict = dictByMajorVerIdx.get( existingKeyMajorVerIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByMajorVerIdx.containsKey( newKeyMajorVerIdx ) ) {
			subdict = dictByMajorVerIdx.get( newKeyMajorVerIdx );
		}
		else {
			subdict = new HashMap< CFLibDbKeyHash256, CFIntBuffMinorVersion >();
			dictByMajorVerIdx.put( newKeyMajorVerIdx, subdict );
		}
		subdict.put( pkey, Buff );

		dictByNameIdx.remove( existingKeyNameIdx );
		dictByNameIdx.put( newKeyNameIdx, Buff );

		return(Buff);
	}

	public void deleteMinorVersion( ICFSecAuthorization Authorization,
		ICFIntMinorVersion Buff )
	{
		final String S_ProcName = "CFIntRamMinorVersionTable.deleteMinorVersion() ";
		String classCode;
		CFLibDbKeyHash256 pkey = schema.getFactoryMinorVersion().newPKey();
		pkey.setRequiredId( Buff.getRequiredId() );
		ICFIntMinorVersion existing = dictByPKey.get( pkey );
		if( existing == null ) {
			return;
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() )
		{
			throw new CFLibCollisionDetectedException( getClass(),
				"deleteMinorVersion",
				pkey );
		}
		CFIntBuffMinorVersionByTenantIdxKey keyTenantIdx = (CFIntBuffMinorVersionByTenantIdxKey)schema.getFactoryMinorVersion().newByTenantIdxKey();
		keyTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffMinorVersionByMajorVerIdxKey keyMajorVerIdx = (CFIntBuffMinorVersionByMajorVerIdxKey)schema.getFactoryMinorVersion().newByMajorVerIdxKey();
		keyMajorVerIdx.setRequiredMajorVersionId( existing.getRequiredMajorVersionId() );

		CFIntBuffMinorVersionByNameIdxKey keyNameIdx = (CFIntBuffMinorVersionByNameIdxKey)schema.getFactoryMinorVersion().newByNameIdxKey();
		keyNameIdx.setRequiredMajorVersionId( existing.getRequiredMajorVersionId() );
		keyNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< CFLibDbKeyHash256, CFIntBuffMinorVersion > subdict;

		dictByPKey.remove( pkey );

		subdict = dictByTenantIdx.get( keyTenantIdx );
		subdict.remove( pkey );

		subdict = dictByMajorVerIdx.get( keyMajorVerIdx );
		subdict.remove( pkey );

		dictByNameIdx.remove( keyNameIdx );

	}
	public void deleteMinorVersionByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		ICFIntMinorVersion cur;
		LinkedList<ICFIntMinorVersion> matchSet = new LinkedList<ICFIntMinorVersion>();
		Iterator<ICFIntMinorVersion> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<ICFIntMinorVersion> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableMinorVersion().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteMinorVersion( Authorization, cur );
		}
	}

	public void deleteMinorVersionByTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTenantId )
	{
		CFIntBuffMinorVersionByTenantIdxKey key = (CFIntBuffMinorVersionByTenantIdxKey)schema.getFactoryMinorVersion().newByTenantIdxKey();
		key.setRequiredTenantId( argTenantId );
		deleteMinorVersionByTenantIdx( Authorization, key );
	}

	public void deleteMinorVersionByTenantIdx( ICFSecAuthorization Authorization,
		ICFIntMinorVersionByTenantIdxKey argKey )
	{
		ICFIntMinorVersion cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<ICFIntMinorVersion> matchSet = new LinkedList<ICFIntMinorVersion>();
		Iterator<ICFIntMinorVersion> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<ICFIntMinorVersion> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableMinorVersion().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteMinorVersion( Authorization, cur );
		}
	}

	public void deleteMinorVersionByMajorVerIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argMajorVersionId )
	{
		CFIntBuffMinorVersionByMajorVerIdxKey key = (CFIntBuffMinorVersionByMajorVerIdxKey)schema.getFactoryMinorVersion().newByMajorVerIdxKey();
		key.setRequiredMajorVersionId( argMajorVersionId );
		deleteMinorVersionByMajorVerIdx( Authorization, key );
	}

	public void deleteMinorVersionByMajorVerIdx( ICFSecAuthorization Authorization,
		ICFIntMinorVersionByMajorVerIdxKey argKey )
	{
		ICFIntMinorVersion cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<ICFIntMinorVersion> matchSet = new LinkedList<ICFIntMinorVersion>();
		Iterator<ICFIntMinorVersion> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<ICFIntMinorVersion> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableMinorVersion().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteMinorVersion( Authorization, cur );
		}
	}

	public void deleteMinorVersionByNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argMajorVersionId,
		String argName )
	{
		CFIntBuffMinorVersionByNameIdxKey key = (CFIntBuffMinorVersionByNameIdxKey)schema.getFactoryMinorVersion().newByNameIdxKey();
		key.setRequiredMajorVersionId( argMajorVersionId );
		key.setRequiredName( argName );
		deleteMinorVersionByNameIdx( Authorization, key );
	}

	public void deleteMinorVersionByNameIdx( ICFSecAuthorization Authorization,
		ICFIntMinorVersionByNameIdxKey argKey )
	{
		ICFIntMinorVersion cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<ICFIntMinorVersion> matchSet = new LinkedList<ICFIntMinorVersion>();
		Iterator<ICFIntMinorVersion> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<ICFIntMinorVersion> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableMinorVersion().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteMinorVersion( Authorization, cur );
		}
	}
}
