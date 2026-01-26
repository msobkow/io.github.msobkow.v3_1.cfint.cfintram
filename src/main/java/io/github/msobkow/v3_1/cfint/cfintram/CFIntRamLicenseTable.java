
// Description: Java 25 in-memory RAM DbIO implementation for License.

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
 *	CFIntRamLicenseTable in-memory RAM DbIO implementation
 *	for License.
 */
public class CFIntRamLicenseTable
	implements ICFIntLicenseTable
{
	private ICFIntSchema schema;
	private Map< CFLibDbKeyHash256,
				CFIntBuffLicense > dictByPKey
		= new HashMap< CFLibDbKeyHash256,
				CFIntBuffLicense >();
	private Map< CFIntBuffLicenseByLicnTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffLicense >> dictByLicnTenantIdx
		= new HashMap< CFIntBuffLicenseByLicnTenantIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffLicense >>();
	private Map< CFIntBuffLicenseByDomainIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffLicense >> dictByDomainIdx
		= new HashMap< CFIntBuffLicenseByDomainIdxKey,
				Map< CFLibDbKeyHash256,
					CFIntBuffLicense >>();
	private Map< CFIntBuffLicenseByUNameIdxKey,
			CFIntBuffLicense > dictByUNameIdx
		= new HashMap< CFIntBuffLicenseByUNameIdxKey,
			CFIntBuffLicense >();

	public CFIntRamLicenseTable( ICFIntSchema argSchema ) {
		schema = argSchema;
	}

	public void createLicense( ICFSecAuthorization Authorization,
		ICFIntLicense Buff )
	{
		final String S_ProcName = "createLicense";
		CFLibDbKeyHash256 pkey = schema.getFactoryLicense().newPKey();
		pkey.setRequiredId( schema.nextLicenseIdGen() );
		Buff.setRequiredId( pkey.getRequiredId() );
		CFIntBuffLicenseByLicnTenantIdxKey keyLicnTenantIdx = schema.getFactoryLicense().newLicnTenantIdxKey();
		keyLicnTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffLicenseByDomainIdxKey keyDomainIdx = schema.getFactoryLicense().newDomainIdxKey();
		keyDomainIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );

		CFIntBuffLicenseByUNameIdxKey keyUNameIdx = schema.getFactoryLicense().newUNameIdxKey();
		keyUNameIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );
		keyUNameIdx.setRequiredName( Buff.getRequiredName() );

		// Validate unique indexes

		if( dictByPKey.containsKey( pkey ) ) {
			throw new CFLibPrimaryKeyNotNewException( getClass(), S_ProcName, pkey );
		}

		if( dictByUNameIdx.containsKey( keyUNameIdx ) ) {
			throw new CFLibUniqueIndexViolationException( getClass(),
				S_ProcName,
				"LicenseUNameIdx",
				keyUNameIdx );
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
						"Owner",
						"Tenant",
						null );
				}
			}
		}

		{
			boolean allNull = true;
			allNull = false;
			if( ! allNull ) {
				if( null == schema.getTableTopDomain().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTopDomainId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						S_ProcName,
						"Container",
						"TopDomain",
						"TopDomain",
						null );
				}
			}
		}

		// Proceed with adding the new record

		dictByPKey.put( pkey, Buff );

		Map< CFLibDbKeyHash256, CFIntBuffLicense > subdictLicnTenantIdx;
		if( dictByLicnTenantIdx.containsKey( keyLicnTenantIdx ) ) {
			subdictLicnTenantIdx = dictByLicnTenantIdx.get( keyLicnTenantIdx );
		}
		else {
			subdictLicnTenantIdx = new HashMap< CFLibDbKeyHash256, CFIntBuffLicense >();
			dictByLicnTenantIdx.put( keyLicnTenantIdx, subdictLicnTenantIdx );
		}
		subdictLicnTenantIdx.put( pkey, Buff );

		Map< CFLibDbKeyHash256, CFIntBuffLicense > subdictDomainIdx;
		if( dictByDomainIdx.containsKey( keyDomainIdx ) ) {
			subdictDomainIdx = dictByDomainIdx.get( keyDomainIdx );
		}
		else {
			subdictDomainIdx = new HashMap< CFLibDbKeyHash256, CFIntBuffLicense >();
			dictByDomainIdx.put( keyDomainIdx, subdictDomainIdx );
		}
		subdictDomainIdx.put( pkey, Buff );

		dictByUNameIdx.put( keyUNameIdx, Buff );

	}

	public ICFIntLicense readDerived( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamLicense.readDerived";
		ICFIntLicense buff;
		if( dictByPKey.containsKey( PKey ) ) {
			buff = dictByPKey.get( PKey );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntLicense lockDerived( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamLicense.readDerived";
		CFLibDbKeyHash256 key = schema.getFactoryLicense().newPKey();
		key.setRequiredId( PKey.getRequiredId() );
		ICFIntLicense buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntLicense[] readAllDerived( ICFSecAuthorization Authorization ) {
		final String S_ProcName = "CFIntRamLicense.readAllDerived";
		ICFIntLicense[] retList = new ICFIntLicense[ dictByPKey.values().size() ];
		Iterator< ICFIntLicense > iter = dictByPKey.values().iterator();
		int idx = 0;
		while( iter.hasNext() ) {
			retList[ idx++ ] = iter.next();
		}
		return( retList );
	}

	public ICFIntLicense[] readDerivedByLicnTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamLicense.readDerivedByLicnTenantIdx";
		CFIntBuffLicenseByLicnTenantIdxKey key = schema.getFactoryLicense().newLicnTenantIdxKey();
		key.setRequiredTenantId( TenantId );

		ICFIntLicense[] recArray;
		if( dictByLicnTenantIdx.containsKey( key ) ) {
			Map< CFLibDbKeyHash256, CFIntBuffLicense > subdictLicnTenantIdx
				= dictByLicnTenantIdx.get( key );
			recArray = new ICFIntLicense[ subdictLicnTenantIdx.size() ];
			Iterator< ICFIntLicense > iter = subdictLicnTenantIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFLibDbKeyHash256, CFIntBuffLicense > subdictLicnTenantIdx
				= new HashMap< CFLibDbKeyHash256, CFIntBuffLicense >();
			dictByLicnTenantIdx.put( key, subdictLicnTenantIdx );
			recArray = new ICFIntLicense[0];
		}
		return( recArray );
	}

	public ICFIntLicense[] readDerivedByDomainIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId )
	{
		final String S_ProcName = "CFIntRamLicense.readDerivedByDomainIdx";
		CFIntBuffLicenseByDomainIdxKey key = schema.getFactoryLicense().newDomainIdxKey();
		key.setRequiredTopDomainId( TopDomainId );

		ICFIntLicense[] recArray;
		if( dictByDomainIdx.containsKey( key ) ) {
			Map< CFLibDbKeyHash256, CFIntBuffLicense > subdictDomainIdx
				= dictByDomainIdx.get( key );
			recArray = new ICFIntLicense[ subdictDomainIdx.size() ];
			Iterator< ICFIntLicense > iter = subdictDomainIdx.values().iterator();
			int idx = 0;
			while( iter.hasNext() ) {
				recArray[ idx++ ] = iter.next();
			}
		}
		else {
			Map< CFLibDbKeyHash256, CFIntBuffLicense > subdictDomainIdx
				= new HashMap< CFLibDbKeyHash256, CFIntBuffLicense >();
			dictByDomainIdx.put( key, subdictDomainIdx );
			recArray = new ICFIntLicense[0];
		}
		return( recArray );
	}

	public ICFIntLicense readDerivedByUNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId,
		String Name )
	{
		final String S_ProcName = "CFIntRamLicense.readDerivedByUNameIdx";
		CFIntBuffLicenseByUNameIdxKey key = schema.getFactoryLicense().newUNameIdxKey();
		key.setRequiredTopDomainId( TopDomainId );
		key.setRequiredName( Name );

		ICFIntLicense buff;
		if( dictByUNameIdx.containsKey( key ) ) {
			buff = dictByUNameIdx.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntLicense readDerivedByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamLicense.readDerivedByIdIdx() ";
		CFLibDbKeyHash256 key = schema.getFactoryLicense().newPKey();
		key.setRequiredId( Id );

		ICFIntLicense buff;
		if( dictByPKey.containsKey( key ) ) {
			buff = dictByPKey.get( key );
		}
		else {
			buff = null;
		}
		return( buff );
	}

	public ICFIntLicense readBuff( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "CFIntRamLicense.readBuff";
		ICFIntLicense buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( ! buff.getClassCode().equals( "a110" ) ) ) {
			buff = null;
		}
		return( buff );
	}

	public ICFIntLicense lockBuff( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 PKey )
	{
		final String S_ProcName = "lockBuff";
		ICFIntLicense buff = readDerived( Authorization, PKey );
		if( ( buff != null ) && ( ! buff.getClassCode().equals( "a110" ) ) ) {
			buff = null;
		}
		return( buff );
	}

	public ICFIntLicense[] readAllBuff( ICFSecAuthorization Authorization )
	{
		final String S_ProcName = "CFIntRamLicense.readAllBuff";
		ICFIntLicense buff;
		ArrayList<ICFIntLicense> filteredList = new ArrayList<ICFIntLicense>();
		ICFIntLicense[] buffList = readAllDerived( Authorization );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && buff.getClassCode().equals( "a110" ) ) {
				filteredList.add( buff );
			}
		}
		return( filteredList.toArray( new ICFIntLicense[0] ) );
	}

	public ICFIntLicense readBuffByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 Id )
	{
		final String S_ProcName = "CFIntRamLicense.readBuffByIdIdx() ";
		ICFIntLicense buff = readDerivedByIdIdx( Authorization,
			Id );
		if( ( buff != null ) && buff.getClassCode().equals( "a110" ) ) {
			return( (ICFIntLicense)buff );
		}
		else {
			return( null );
		}
	}

	public ICFIntLicense[] readBuffByLicnTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TenantId )
	{
		final String S_ProcName = "CFIntRamLicense.readBuffByLicnTenantIdx() ";
		ICFIntLicense buff;
		ArrayList<ICFIntLicense> filteredList = new ArrayList<ICFIntLicense>();
		ICFIntLicense[] buffList = readDerivedByLicnTenantIdx( Authorization,
			TenantId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && buff.getClassCode().equals( "a110" ) ) {
				filteredList.add( (ICFIntLicense)buff );
			}
		}
		return( filteredList.toArray( new ICFIntLicense[0] ) );
	}

	public ICFIntLicense[] readBuffByDomainIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId )
	{
		final String S_ProcName = "CFIntRamLicense.readBuffByDomainIdx() ";
		ICFIntLicense buff;
		ArrayList<ICFIntLicense> filteredList = new ArrayList<ICFIntLicense>();
		ICFIntLicense[] buffList = readDerivedByDomainIdx( Authorization,
			TopDomainId );
		for( int idx = 0; idx < buffList.length; idx ++ ) {
			buff = buffList[idx];
			if( ( buff != null ) && buff.getClassCode().equals( "a110" ) ) {
				filteredList.add( (ICFIntLicense)buff );
			}
		}
		return( filteredList.toArray( new ICFIntLicense[0] ) );
	}

	public ICFIntLicense readBuffByUNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 TopDomainId,
		String Name )
	{
		final String S_ProcName = "CFIntRamLicense.readBuffByUNameIdx() ";
		ICFIntLicense buff = readDerivedByUNameIdx( Authorization,
			TopDomainId,
			Name );
		if( ( buff != null ) && buff.getClassCode().equals( "a110" ) ) {
			return( (ICFIntLicense)buff );
		}
		else {
			return( null );
		}
	}

	public void updateLicense( ICFSecAuthorization Authorization,
		ICFIntLicense Buff )
	{
		CFLibDbKeyHash256 pkey = schema.getFactoryLicense().newPKey();
		pkey.setRequiredId( Buff.getRequiredId() );
		ICFIntLicense existing = dictByPKey.get( pkey );
		if( existing == null ) {
			throw new CFLibStaleCacheDetectedException( getClass(),
				"updateLicense",
				"Existing record not found",
				"License",
				pkey );
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() ) {
			throw new CFLibCollisionDetectedException( getClass(),
				"updateLicense",
				pkey );
		}
		Buff.setRequiredRevision( Buff.getRequiredRevision() + 1 );
		CFIntBuffLicenseByLicnTenantIdxKey existingKeyLicnTenantIdx = schema.getFactoryLicense().newLicnTenantIdxKey();
		existingKeyLicnTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffLicenseByLicnTenantIdxKey newKeyLicnTenantIdx = schema.getFactoryLicense().newLicnTenantIdxKey();
		newKeyLicnTenantIdx.setRequiredTenantId( Buff.getRequiredTenantId() );

		CFIntBuffLicenseByDomainIdxKey existingKeyDomainIdx = schema.getFactoryLicense().newDomainIdxKey();
		existingKeyDomainIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );

		CFIntBuffLicenseByDomainIdxKey newKeyDomainIdx = schema.getFactoryLicense().newDomainIdxKey();
		newKeyDomainIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );

		CFIntBuffLicenseByUNameIdxKey existingKeyUNameIdx = schema.getFactoryLicense().newUNameIdxKey();
		existingKeyUNameIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );
		existingKeyUNameIdx.setRequiredName( existing.getRequiredName() );

		CFIntBuffLicenseByUNameIdxKey newKeyUNameIdx = schema.getFactoryLicense().newUNameIdxKey();
		newKeyUNameIdx.setRequiredTopDomainId( Buff.getRequiredTopDomainId() );
		newKeyUNameIdx.setRequiredName( Buff.getRequiredName() );

		// Check unique indexes

		if( ! existingKeyUNameIdx.equals( newKeyUNameIdx ) ) {
			if( dictByUNameIdx.containsKey( newKeyUNameIdx ) ) {
				throw new CFLibUniqueIndexViolationException( getClass(),
					"updateLicense",
					"LicenseUNameIdx",
					newKeyUNameIdx );
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
						"updateLicense",
						"Owner",
						"Owner",
						"Tenant",
						null );
				}
			}
		}

		{
			boolean allNull = true;

			if( allNull ) {
				if( null == schema.getTableTopDomain().readDerivedByIdIdx( Authorization,
						Buff.getRequiredTopDomainId() ) )
				{
					throw new CFLibUnresolvedRelationException( getClass(),
						"updateLicense",
						"Container",
						"TopDomain",
						"TopDomain",
						null );
				}
			}
		}

		// Update is valid

		Map< CFLibDbKeyHash256, CFIntBuffLicense > subdict;

		dictByPKey.remove( pkey );
		dictByPKey.put( pkey, Buff );

		subdict = dictByLicnTenantIdx.get( existingKeyLicnTenantIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByLicnTenantIdx.containsKey( newKeyLicnTenantIdx ) ) {
			subdict = dictByLicnTenantIdx.get( newKeyLicnTenantIdx );
		}
		else {
			subdict = new HashMap< CFLibDbKeyHash256, CFIntBuffLicense >();
			dictByLicnTenantIdx.put( newKeyLicnTenantIdx, subdict );
		}
		subdict.put( pkey, Buff );

		subdict = dictByDomainIdx.get( existingKeyDomainIdx );
		if( subdict != null ) {
			subdict.remove( pkey );
		}
		if( dictByDomainIdx.containsKey( newKeyDomainIdx ) ) {
			subdict = dictByDomainIdx.get( newKeyDomainIdx );
		}
		else {
			subdict = new HashMap< CFLibDbKeyHash256, CFIntBuffLicense >();
			dictByDomainIdx.put( newKeyDomainIdx, subdict );
		}
		subdict.put( pkey, Buff );

		dictByUNameIdx.remove( existingKeyUNameIdx );
		dictByUNameIdx.put( newKeyUNameIdx, Buff );

	}

	public void deleteLicense( ICFSecAuthorization Authorization,
		ICFIntLicense Buff )
	{
		final String S_ProcName = "CFIntRamLicenseTable.deleteLicense() ";
		String classCode;
		CFLibDbKeyHash256 pkey = schema.getFactoryLicense().newPKey();
		pkey.setRequiredId( Buff.getRequiredId() );
		ICFIntLicense existing = dictByPKey.get( pkey );
		if( existing == null ) {
			return;
		}
		if( existing.getRequiredRevision() != Buff.getRequiredRevision() )
		{
			throw new CFLibCollisionDetectedException( getClass(),
				"deleteLicense",
				pkey );
		}
		CFIntBuffLicenseByLicnTenantIdxKey keyLicnTenantIdx = schema.getFactoryLicense().newLicnTenantIdxKey();
		keyLicnTenantIdx.setRequiredTenantId( existing.getRequiredTenantId() );

		CFIntBuffLicenseByDomainIdxKey keyDomainIdx = schema.getFactoryLicense().newDomainIdxKey();
		keyDomainIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );

		CFIntBuffLicenseByUNameIdxKey keyUNameIdx = schema.getFactoryLicense().newUNameIdxKey();
		keyUNameIdx.setRequiredTopDomainId( existing.getRequiredTopDomainId() );
		keyUNameIdx.setRequiredName( existing.getRequiredName() );

		// Validate reverse foreign keys

		// Delete is valid
		Map< CFLibDbKeyHash256, CFIntBuffLicense > subdict;

		dictByPKey.remove( pkey );

		subdict = dictByLicnTenantIdx.get( keyLicnTenantIdx );
		subdict.remove( pkey );

		subdict = dictByDomainIdx.get( keyDomainIdx );
		subdict.remove( pkey );

		dictByUNameIdx.remove( keyUNameIdx );

	}
	public void deleteLicenseByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argId )
	{
		CFLibDbKeyHash256 key = schema.getFactoryLicense().newPKey();
		key.setRequiredId( argId );
		deleteLicenseByIdIdx( Authorization, key );
	}

	public void deleteLicenseByIdIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argKey )
	{
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		ICFIntLicense cur;
		LinkedList<ICFIntLicense> matchSet = new LinkedList<ICFIntLicense>();
		Iterator<ICFIntLicense> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<ICFIntLicense> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableLicense().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteLicense( Authorization, cur );
		}
	}

	public void deleteLicenseByLicnTenantIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTenantId )
	{
		CFIntBuffLicenseByLicnTenantIdxKey key = schema.getFactoryLicense().newLicnTenantIdxKey();
		key.setRequiredTenantId( argTenantId );
		deleteLicenseByLicnTenantIdx( Authorization, key );
	}

	public void deleteLicenseByLicnTenantIdx( ICFSecAuthorization Authorization,
		ICFIntLicenseByLicnTenantIdxKey argKey )
	{
		ICFIntLicense cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<ICFIntLicense> matchSet = new LinkedList<ICFIntLicense>();
		Iterator<ICFIntLicense> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<ICFIntLicense> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableLicense().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteLicense( Authorization, cur );
		}
	}

	public void deleteLicenseByDomainIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTopDomainId )
	{
		CFIntBuffLicenseByDomainIdxKey key = schema.getFactoryLicense().newDomainIdxKey();
		key.setRequiredTopDomainId( argTopDomainId );
		deleteLicenseByDomainIdx( Authorization, key );
	}

	public void deleteLicenseByDomainIdx( ICFSecAuthorization Authorization,
		ICFIntLicenseByDomainIdxKey argKey )
	{
		ICFIntLicense cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<ICFIntLicense> matchSet = new LinkedList<ICFIntLicense>();
		Iterator<ICFIntLicense> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<ICFIntLicense> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableLicense().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteLicense( Authorization, cur );
		}
	}

	public void deleteLicenseByUNameIdx( ICFSecAuthorization Authorization,
		CFLibDbKeyHash256 argTopDomainId,
		String argName )
	{
		CFIntBuffLicenseByUNameIdxKey key = schema.getFactoryLicense().newUNameIdxKey();
		key.setRequiredTopDomainId( argTopDomainId );
		key.setRequiredName( argName );
		deleteLicenseByUNameIdx( Authorization, key );
	}

	public void deleteLicenseByUNameIdx( ICFSecAuthorization Authorization,
		ICFIntLicenseByUNameIdxKey argKey )
	{
		ICFIntLicense cur;
		boolean anyNotNull = false;
		anyNotNull = true;
		anyNotNull = true;
		if( ! anyNotNull ) {
			return;
		}
		LinkedList<ICFIntLicense> matchSet = new LinkedList<ICFIntLicense>();
		Iterator<ICFIntLicense> values = dictByPKey.values().iterator();
		while( values.hasNext() ) {
			cur = values.next();
			if( argKey.equals( cur ) ) {
				matchSet.add( cur );
			}
		}
		Iterator<ICFIntLicense> iterMatch = matchSet.iterator();
		while( iterMatch.hasNext() ) {
			cur = iterMatch.next();
			cur = schema.getTableLicense().readDerivedByIdIdx( Authorization,
				cur.getRequiredId() );
			deleteLicense( Authorization, cur );
		}
	}
}
